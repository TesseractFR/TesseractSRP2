package onl.tesseract.srp.domain.money.ledger;

public interface TransactionSubType {

    enum Job implements TransactionSubType {
        Forgeron, Bucheron
    }

    enum Staff implements TransactionSubType {
        Give
    }

    enum Player implements TransactionSubType {
        Rank
    }

    enum Guild implements TransactionSubType {
        Creation, BankTransfer
    }
}
