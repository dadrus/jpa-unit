insert into DEPOSITOR (ID, NAME, SURNAME, VERSION) values (1, 'Max', 'Payne', 1);
insert into ACCOUNT (ID, DEPOSITOR_ID, VERSION, CREDIT_LIMIT, TYPE) values (1, 1, 1, 100000.0, 'GIRO_ACCOUNT');
insert into ACCOUNT_ENTRY (ID, ACCOUNT_ID, AMOUNT, DATE, DETAILS, REFERENCE, TYPE) values (1, 1, 0.0, NOW(), 'deposit', 'ACC', 'DEBIT');
insert into ACCOUNT_ENTRY (ID, ACCOUNT_ID, AMOUNT, DATE, DETAILS, REFERENCE, TYPE) values (2, 1, 100000.0, NOW(), 'deposit', 'ACC', 'DEBIT');
insert into ADDRESS (ID, DEPOSITOR_ID, CITY, COUNTRY, STREET, ZIP_CODE) values (1, 1, 'Unknown', 'Unknown', 'Unknown', '111111');
insert into CONTACT_DETAIL (ID, DEPOSITOR_ID, TYPE, VALUE) values (1, 1, 'EMAIL', 'max@payne.com');