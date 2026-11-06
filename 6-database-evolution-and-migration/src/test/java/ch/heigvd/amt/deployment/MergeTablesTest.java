package ch.heigvd.amt.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Postgres;

/**
 * La fusion de deux tables. Pendant la transition, l'ancien code écrit employee_identification, le
 * nouveau écrit employee, et un trigger de chaque côté recopie ces écritures dans l'autre table.
 */
class MergeTablesTest {

    @BeforeEach
    void expand() throws SQLException {
        Postgres.emptyDatabase();
        Postgres.execute("""
                CREATE TABLE employee (
                    employee_number serial PRIMARY KEY,
                    name text NOT NULL,
                    phone_number text
                );
                CREATE TABLE employee_identification (
                    employee_number integer PRIMARY KEY REFERENCES employee ON DELETE CASCADE,
                    picture text,
                    voice_print text,
                    retinal_print text
                );
                INSERT INTO employee (name) VALUES ('Ada Lovelace'), ('Grace Hopper');
                INSERT INTO employee_identification (employee_number, picture) VALUES (1, 'ada.png')""");

        Postgres.execute("""
                ALTER TABLE employee
                    ADD COLUMN picture text,
                    ADD COLUMN voice_print text,
                    ADD COLUMN retinal_print text;
                UPDATE employee
                SET picture = identification.picture,
                    voice_print = identification.voice_print,
                    retinal_print = identification.retinal_print
                FROM employee_identification identification
                WHERE identification.employee_number = employee.employee_number;

                -- pg_trigger_depth : sans ce garde, chaque trigger relancerait l'autre sans fin.
                -- À la suppression d'une ligne, NEW est vide : le trigger efface les colonnes.
                CREATE FUNCTION synchronize_with_employee() RETURNS trigger
                LANGUAGE plpgsql AS $$
                BEGIN
                    IF pg_trigger_depth() = 1 THEN
                        UPDATE employee
                        SET picture = NEW.picture,
                            voice_print = NEW.voice_print,
                            retinal_print = NEW.retinal_print
                        WHERE employee_number = coalesce(NEW.employee_number, OLD.employee_number);
                    END IF;
                    RETURN NULL;
                END;
                $$;

                CREATE TRIGGER synchronize_with_employee
                    AFTER INSERT OR UPDATE OR DELETE ON employee_identification
                    FOR EACH ROW
                    EXECUTE FUNCTION synchronize_with_employee();

                CREATE FUNCTION synchronize_with_employee_identification() RETURNS trigger
                LANGUAGE plpgsql AS $$
                BEGIN
                    IF pg_trigger_depth() = 1 THEN
                        INSERT INTO employee_identification (employee_number, picture, voice_print, retinal_print)
                        VALUES (NEW.employee_number, NEW.picture, NEW.voice_print, NEW.retinal_print)
                        ON CONFLICT (employee_number) DO UPDATE
                        SET picture = EXCLUDED.picture,
                            voice_print = EXCLUDED.voice_print,
                            retinal_print = EXCLUDED.retinal_print;
                    END IF;
                    RETURN NULL;
                END;
                $$;

                CREATE TRIGGER synchronize_with_employee_identification
                    AFTER INSERT OR UPDATE ON employee
                    FOR EACH ROW
                    EXECUTE FUNCTION synchronize_with_employee_identification()""");
    }

    @Test
    void theExpandCopiesTheExistingRows() throws SQLException {
        assertEquals("ada.png", Postgres.queryValue("SELECT picture FROM employee WHERE employee_number = 1"));
    }

    @Test
    void anInsertByTheOldCodeReachesEmployee() throws SQLException {
        Postgres.execute("INSERT INTO employee_identification (employee_number, picture) VALUES (2, 'grace.png')");

        assertEquals("grace.png", Postgres.queryValue("SELECT picture FROM employee WHERE employee_number = 2"));
    }

    @Test
    void anUpdateByTheOldCodeReachesEmployee() throws SQLException {
        Postgres.execute("UPDATE employee_identification SET voice_print = 'ada.wav' WHERE employee_number = 1");

        assertEquals("ada.wav", Postgres.queryValue("SELECT voice_print FROM employee WHERE employee_number = 1"));
    }

    @Test
    void aDeleteByTheOldCodeClearsTheColumnsOfEmployee() throws SQLException {
        Postgres.execute("DELETE FROM employee_identification WHERE employee_number = 1");

        assertNull(Postgres.queryValue("SELECT picture FROM employee WHERE employee_number = 1"));
    }

    @Test
    void anUpdateByTheNewCodeReachesEmployeeIdentification() throws SQLException {
        Postgres.execute("UPDATE employee SET retinal_print = 'ada.iris' WHERE employee_number = 1");

        assertEquals("ada.iris",
            Postgres.queryValue("SELECT retinal_print FROM employee_identification WHERE employee_number = 1"));
    }

    @Test
    void anInsertByTheNewCodeReachesEmployeeIdentification() throws SQLException {
        Postgres.execute("INSERT INTO employee (name, picture) VALUES ('Alan Turing', 'alan.png')");

        assertEquals("alan.png",
            Postgres.queryValue("SELECT picture FROM employee_identification WHERE employee_number = 3"));
    }

    @Test
    void aDeleteByTheNewCodeRemovesBothRows() throws SQLException {
        Postgres.execute("DELETE FROM employee WHERE employee_number = 1");

        assertEquals(0L, Postgres.queryValue("SELECT count(*) FROM employee_identification WHERE employee_number = 1"));
    }

    @Test
    void theContractDropsTheTableAndKeepsTheData() throws SQLException {
        Postgres.execute("""
                DROP TABLE employee_identification;
                DROP TRIGGER synchronize_with_employee_identification ON employee;
                DROP FUNCTION synchronize_with_employee_identification();
                DROP FUNCTION synchronize_with_employee()""");

        assertFalse(Postgres.tableExists("employee_identification"));
        assertEquals("ada.png", Postgres.queryValue("SELECT picture FROM employee WHERE employee_number = 1"));
    }
}
