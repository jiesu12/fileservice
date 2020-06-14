# Fileservice
## Search Function
### Endpoints
Path is relative to Fileservice base directory.
* Index File
```shell script
curl -X POST http://localhost:8100/api/search?path=test.txt
```
* Search by keyword
```shell script
curl http://localhost:8100/api/search?keyword=foo
```
* Delete file index
```shell script
curl -X DELETE http://localhost:8100/api/search?path=test.txt
```

### Install Elasticsearch
* Download <https://www.elastic.co/downloads/elasticsearch>, click `LINUX X86_64` one.
* Extract
* run `bin/elasticsearch`

### Elasticsearch Endpoints
* `curl http://localhost:9200/file/_search?q=keyword`
