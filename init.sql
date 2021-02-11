CREATE TABLE topfilm (
	title varchar PRIMARY KEY,
	budget float4 NOT NULL,
	"year" int4 NULL,
	revenue float4 NOT NULL,
	rating float4 NOT NULL,
	genres varchar NULL,
	productioncompany varchar NULL,
	wikilink varchar NULL,
	wikiabstract varchar NULL,
	ratio float4 NOT NULL
);