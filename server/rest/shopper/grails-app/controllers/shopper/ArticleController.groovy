package shopper

import grails.converters.JSON

class ArticleController {

    def index() {
        def article = Article.findByBarcode(params.barcode)
        JSON.use('deep')
        render article as JSON
    }

    def create() {
        def json = request.JSON

        def article = new Article(name: json.name,
        description: json.description,
        barcode: json.barcode).save(flush: true)

        render article as JSON
    }
}
