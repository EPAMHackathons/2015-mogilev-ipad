package shopper

class Article {

    String barcode
    String name
    String description
    List<ArticleInStore> articleInStoreList = []

    static hasMany = [
            articleInStoreList: ArticleInStore
    ]

    static constraints = {
        barcode nullable: false, blank: false, unique: true
        name nullable: false, blank: false, maxSize: 1000
        description nullable: false, blank: false, maxSize: 10000
    }
}
