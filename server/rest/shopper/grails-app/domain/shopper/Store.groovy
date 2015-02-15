package shopper

class Store {

    String name
    String description
    User user
    List<Place> places = []

    static hasMany = [
            places: Place
    ]

    static constraints = {
        name nullable: false, blank: false, maxSize: 1000
        description nullable: false, blank: false, maxSize: 10000
    }
}