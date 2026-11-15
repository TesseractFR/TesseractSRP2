# Reste à faire — `contract-domain`

## Valeur des items (en attente)

- [ ] S'assurer que chaque item récoltable ou fabriqué qui peut sortir d'un contrat a un prix dans `itemValue.yml` : sans prix, l'adapter l'exclut du catalogue (avertissement dans le journal).

## Règles à décider

- [ ] Comment gagner des tickets en jeu (vote, loot, activité) : aucune méthode de gain n'existe pour l'instant, seule la commande staff ajoute des tickets.
- [ ] Parchemin de découverte du tutoriel : à gérer dans le tutoriel, pas dans ce module.
- [ ] Retirer des tickets en staff : `ContractTicketService.withdrawTicket` existe mais n'est pas exposé par une commande, et il ne vérifie pas que le solde reste positif.
- [ ] `renewDaily` ramène au quota un solde supérieur (tickets donnés par le staff perdus le lendemain) : confirmer que c'est voulu.
- [ ] Borne maximale de quantité : aucune aujourd'hui, un contrat peut demander plusieurs centaines d'items.
- [ ] Quantité des contrats d'artisanat : elle vient de `quantity-by-rarity` et de `quantity-variance-percent`, comme la récolte ; vérifier l'équilibre.

## Après la mise en place

- [ ] Aligner les specs voisines (`09-guildes`, `10-rivalite-et-recrutement`, `16-decisions-et-todo`) avec les décisions de cette spec.
