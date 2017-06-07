db.DEPOSITOR.insertOne({
	"_id": 1,
	"NAME": "Max",
	"SURNAME": "Payne",
	"VERSION": 1,
	"addresses": [ 1 ],
	"accounts": [ 1 ],
	"contactDetails" : [ 1 ]
})

db.ACCOUNT.insertOne({
	"_id": 1,
	"DEPOSITOR_ID": 1,
	"VERSION": 1,
	"CREDIT_LIMIT": 100000.0,
	"TYPE": "GIRO_ACCOUNT",
	"entries": [1, 2]
})

db.ACCOUNT_ENTRY.insertMany([
	{
		"_id": 1,
		"ACCOUNT_ID": 1,
		"AMOUNT": 0.0,
		"DATE": new Date(),
		"DETAILS": "deposit",
		"REFERENCE": "ACC",
		"TYPE": "DEBIT"
	},
	{
		"_id": 2,
		"ACCOUNT_ID": 1,
		"AMOUNT": 100000.0,
		"DATE": new Date(),
		"DETAILS": "deposit",
		"REFERENCE": "ACC",
		"TYPE": "DEBIT"
	}
])

db.ADDRESS.insertOne({
	"_id": 1,
	"DEPOSITOR_ID": 1,
	"CITY": "Unknown",
	"COUNTRY": "Unknown",
	"STREET": "Unknown",
	"ZIP_CODE": "111111"
})

db.CONTACT_DETAIL.insertOne({
	"_id": 1,
	"DEPOSITOR_ID": 1,
	"TYPE": "EMAIL",
	"VALUE": "max@payne.com"
})