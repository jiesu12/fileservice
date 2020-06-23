# Fileservice

## Search Function

### Endpoints

Path is relative to Fileservice base directory.

- Index File

```shell script
curl -X POST http://localhost:8100/api/search?path=test.txt
```

- Search by keyword

```shell script
curl http://localhost:8100/api/search?keyword=foo
```

- Delete file index

```shell script
curl -X DELETE http://localhost:8100/api/search?path=test.txt
```

### Elasticsearch

##### Install

- Download <https://www.elastic.co/downloads/elasticsearch>, click `LINUX X86_64` one.
- Extract
- run `bin/elasticsearch`

##### Setup

Update `config/elasticsearch.yml`:

```yaml
# by default only localhost can be used to call elasticsearch endpoints, this setting allows using IP or DNS name.
network.host: 0.0.0.0
# Needed if network.host is set.
discovery.seed_hosts: ["127.0.0.1"]
# Without this, it would require increasing Linux mmapfs size.
# https://www.elastic.co/guide/en/elasticsearch/reference/current/vm-max-map-count.html#vm-max-map-count
node.store.allow_mmap: false
```

##### Elasticsearch Endpoints

- `curl http://localhost:9200/file/_search?q=keyword`

