gunzip --keep photo.jsonl.gz
mongoimport --uri mongodb://localhost --db AtlasSearch --collection photo --drop --file photo.jsonl
./create-index.sh