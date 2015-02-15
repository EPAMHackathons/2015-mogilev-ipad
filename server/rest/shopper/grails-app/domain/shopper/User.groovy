package shopper

class User {

    String googleId
    String name
    String firstName
    String lastName
    String avatarLink
    String gplusAccountLink
    String locale
    String gender

    static constraints = {
        googleId nullable: false, blank: false
        name nullable: false, blank: false
        firstName nullable: true, blank: true
        lastName nullable: true, blank: true
        avatarLink nullable: true, blank: false, maxSize: 10000
        gplusAccountLink nullable: true, blank: false, maxSize: 10000
        locale nullable: true, blank: false
        gender nullable: true, blank: false
    }

    static mapping = {
        table 'users'
    }
}
