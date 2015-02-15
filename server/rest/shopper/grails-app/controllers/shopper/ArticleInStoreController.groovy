package shopper

import grails.converters.JSON

class ArticleInStoreController {

    def loginService

    def index() {
        def article = Article.findByBarcode(params.barcode)
        render article.articleInStoreList as JSON
    }

    def create() {
        def json = request.JSON
        def user = User.get(loginService.findToken(json.token).userId)
        def store = Store.get(json.storeId as long)
        def article = Article.get(json.articleId as long)
        def price = json.price as double
        def articleInStore = new ArticleInStore(user: user,
                                                store: store,
                                                article: article,
                                                price: price)
        article.addToArticleInStoreList(articleInStore)
        article = article.save(flush: true)

        render articleInStore as JSON
    }
}
