package com.mainframe.carddemo.migration.extensions;

/**
 * Documents compatibility between optional COBOL extensions and the migrated Postgres schema.
 *
 * <h2>DB2 Transaction Types (app-transaction-type-db2)</h2>
 * The DB2 extension tables map directly to existing Postgres tables:
 * <ul>
 *   <li>{@code CARDDEMO.TRANSACTION_TYPE (TR_TYPE, TR_DESCRIPTION)}
 *       → {@code tran_type (tran_type CHAR(2) PK, tran_type_desc VARCHAR(50))}</li>
 *   <li>{@code CARDDEMO.TRANSACTION_TYPE_CATEGORY (TRC_TYPE_CODE, TRC_TYPE_CATEGORY, TRC_CAT_DATA)}
 *       → {@code tran_category (tran_type_cd CHAR(2), tran_cat_cd INT, tran_cat_type_desc VARCHAR(50))}</li>
 * </ul>
 * No additional migration is needed; the existing MigrationJob loads these tables
 * from {@code trantype.txt} and {@code trancatg.txt}.
 *
 * <h2>IMS DB Authorization (app-authorization-ims-db2-mq)</h2>
 * The IMS extension stores authorization fraud records in a hierarchical IMS DB
 * (DBPAUTP0) with EBCDIC-encoded segments. The corresponding DB2 staging table is
 * {@code CARDDEMO.AUTHFRDS}. Flattening the IMS hierarchy requires:
 * <ol>
 *   <li>EBCDIC-to-ASCII conversion of the binary IMS dump</li>
 *   <li>Parsing IMS segment headers (prefix bytes, segment codes)</li>
 *   <li>Extracting AUTHFRDS fields per the DBD/PSB definitions</li>
 * </ol>
 * A Flyway migration {@code V7__auth_fraud.sql} should create the Postgres equivalent:
 * <pre>{@code
 * CREATE TABLE IF NOT EXISTS auth_fraud (
 *     card_num       CHAR(16) NOT NULL,
 *     auth_ts        TIMESTAMP NOT NULL,
 *     auth_type      CHAR(4),
 *     auth_resp_code CHAR(2),
 *     transaction_amt DECIMAL(12,2),
 *     approved_amt   DECIMAL(12,2),
 *     merchant_id    CHAR(15),
 *     auth_fraud     CHAR(1),
 *     acct_id        BIGINT,
 *     cust_id        BIGINT,
 *     PRIMARY KEY (card_num, auth_ts)
 * );
 * }</pre>
 * The IMS binary parser is deferred to a future phase due to EBCDIC encoding complexity.
 */
public final class ExtensionCompatibility {
    private ExtensionCompatibility() {}
}
