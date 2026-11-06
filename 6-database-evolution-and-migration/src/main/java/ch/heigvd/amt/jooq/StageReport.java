package ch.heigvd.amt.jooq;

import static ch.heigvd.amt.db.Tables.CONCERT;
import static ch.heigvd.amt.db.Tables.STAGE;
import static org.jooq.impl.DSL.count;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Result;

/** Le nombre de concerts par scène, écrit avec les classes que jOOQ a générées depuis la base. */
public class StageReport {

    private final DSLContext dsl;

    public StageReport(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Result<Record2<String, Integer>> concertsPerStage() {
        var total = count(CONCERT.ID).as("total");

        Result<Record2<String, Integer>> rows = dsl
            .select(STAGE.NAME, total)
            .from(STAGE)
            .join(CONCERT).on(CONCERT.STAGE_ID.eq(STAGE.ID))
            .groupBy(STAGE.ID, STAGE.NAME)
            .orderBy(total.desc())
            .fetch();
        return rows;
    }
}
