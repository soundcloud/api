# API Public Release Notes

## Unsupported track transcodings

- fixes https://github.com/soundcloud/api/issues/534 where unplayable tracks were returned as playable but then 403 was returned when trying to play 
- The `/streams` JSON payload **no longer emits keys with null values**. Optional URL fields are **omitted entirely** when not applicable, instead of `"field": null`.

<!--- Remove everything below and start over --->


