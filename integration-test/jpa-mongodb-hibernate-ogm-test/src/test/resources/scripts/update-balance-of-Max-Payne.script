db.ACCOUNT_ENTRY.insertOne({
	"_id": NumberLong(3),
	"ACCOUNT_ID": NumberLong(1),
	"AMOUNT": -5000.0,
	"DATE": new Date(),
	"DETAILS": "cash withdraw",
	"REFERENCE": "ACC",
	"TYPE": "CREDIT"
})

db.ACCOUNT.findAndModify({
    query: { "_id": NumberLong(1) },
    update: { $push: { "entries": NumberLong(3) } }
})
