package org.example

static void main(String[] args) {
    def crawler = new WebCrawler()
    if (crawler) {
        crawler.downloadComunicationComponent()
        crawler.collectVersionHistory()
        crawler.downloadSendingErrorsTable()
    }
}