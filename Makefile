all:

dev:
	clojure -X blog-clojure.core/start-server

build:
	clojure -X blog-clojure.core/export
