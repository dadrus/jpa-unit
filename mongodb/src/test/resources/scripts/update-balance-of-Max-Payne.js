db.ACCOUNT_ENTRY.insertOne({
	"_id": 3,
	"ACCOUNT_ID": 1,
	"AMOUNT": -5000.0,
	"DATE": new Date(),
	"DETAILS": "cash withdraw",
	"REFERENCE": "ACC",
	"TYPE": "CREDIT"
})

db.ACCOUNT.findAndModify({
    query: { "_id": 1 },
    update: { $push: { "entries": 3 } }
})
