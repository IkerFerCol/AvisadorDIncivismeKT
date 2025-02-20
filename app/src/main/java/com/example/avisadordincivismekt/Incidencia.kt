class Incidencia {
    var latitud: String? = null
    var longitud: String? = null
    var direccio: String? = null
    var problema: String? = null
    var url: String? = null

    constructor(
        latitud: String?, longitud: String?, direccio: String?,
        problema: String?, url: String?
    ) {
        this.latitud = latitud
        this.longitud = longitud
        this.direccio = direccio
        this.problema = problema
        this.url = url
    }

    constructor()
}