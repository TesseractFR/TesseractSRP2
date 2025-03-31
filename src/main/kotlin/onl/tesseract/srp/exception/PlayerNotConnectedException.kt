package onl.tesseract.srp.exception

class PlayerNotConnectedException : RuntimeException {
    constructor() : super()
    constructor(message: String?) : super(message)
}