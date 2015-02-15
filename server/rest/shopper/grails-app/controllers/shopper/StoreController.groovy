package shopper

import grails.converters.JSON

class StoreController {

    public static final double RADIUS = 0.0005
    def loginService

    def index() {
        def longitude = params.lg as double
        def latitude = params.lt as double

        def stores = Store.list().findAll { store ->
            store.places.any { place ->
                place.latitude - RADIUS < latitude && latitude < place.latitude + RADIUS &&
                place.longitude - RADIUS < longitude && longitude < place.longitude + RADIUS
            }
        }

        JSON.use("deep")

        render stores as JSON
    }

    def list() {
        render Store.list() as JSON
    }

    def create() {
        def json = request.JSON
        def user = User.get(loginService.findToken(json.token).userId)
        def place = new Place(longitude: json.longitude as double,
                              latitude: json.latitude as double,
                              user: user)

        def store = new Store(name: json.name,
                              description: json.description,
                              user: user,
                              places: []).save(flush: true)
        store.addToPlaces(place)
        store = store.save(flush: true)

        JSON.use("deep")
        render store as JSON
    }
}
