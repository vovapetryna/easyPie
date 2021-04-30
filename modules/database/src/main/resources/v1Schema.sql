CREATE TABLE "accounts"
(
    "id"       SERIAL       NOT NULL,
    "name"     VARCHAR(256) NOT NULL,
    "email"    VARCHAR(256) NOT NULL,
    "password" VARCHAR(256) NOT NULL,
    CONSTRAINT "accounts_pk" PRIMARY KEY ("id")
);
