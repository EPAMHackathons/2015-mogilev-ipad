package shopper

class Place {

    User user
    double longitude
    double latitude

    static belongsTo = [
            store: Store
    ]

    static constraints = {
    }
}
