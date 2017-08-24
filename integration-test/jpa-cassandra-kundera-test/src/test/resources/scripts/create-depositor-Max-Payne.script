db.DEPOSITOR.insertOne({
	"_id": NumberLong(1),
	"NAME": "Max",
	"SURNAME": "Payne",
	"VERSION": NumberLong(1),
	"addresses": [ NumberLong(1) ],
	"accounts": [ NumberLong(1) ],
	"contactDetails" : [ NumberLong(1) ]
})

db.ACCOUNT.insertOne({
	"_id": NumberLong(1),
	"DEPOSITOR_ID": NumberLong(1),
	"VERSION": NumberLong(1),
	"CREDIT_LIMIT": 100000.0,
	"TYPE": "GIRO_ACCOUNT",
	"entries": [NumberLong(1), NumberLong(2)]
})

db.ACCOUNT_ENTRY.insertMany([
	{
		"_id": NumberLong(1),
		"ACCOUNT_ID": NumberLong(1),
		"AMOUNT": 0.0,
		"DATE": new Date(),
		"DETAILS": "deposit",
		"REFERENCE": "ACC",
		"TYPE": "DEBIT"
	},
	{
		"_id": NumberLong(2),
		"ACCOUNT_ID": NumberLong(1),
		"AMOUNT": 100000.0,
		"DATE": new Date(),
		"DETAILS": "deposit",
		"REFERENCE": "ACC",
		"TYPE": "DEBIT"
	}
])

db.ADDRESS.insertOne({
	"_id": NumberLong(1),
	"DEPOSITOR_ID": NumberLong(1),
	"CITY": "Unknown",
	"COUNTRY": "Unknown",
	"STREET": "Unknown",
	"ZIP_CODE": "111111"
})

db.CONTACT_DETAIL.insertOne({
	"_id": NumberLong(1),
	"DEPOSITOR_ID": NumberLong(1),
	"TYPE": "EMAIL",
	"VALUE": "max@payne.com"
})