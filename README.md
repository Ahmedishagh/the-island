# The Island – Projet POO IATIC3 2025/2026

Implémentation Java/Swing du jeu de société **The Island**.  
Réalisé dans le cadre du cours d'Approche Orientée Objet — IATIC 3.

---

## Lancer le jeu

```bash
# Compiler
javac -d target/classes $(find src -name "*.java")

# Lancer
java -jar target/the-island.jar
```

Ou avec Maven si disponible :
```bash
mvn package
java -jar target/the-island.jar
```

---

## Structure du projet

```
src/main/java/theisland/
├── Main.java                        # Point d'entrée
│
├── util/
│   └── HexCoord.java                # Coordonnées hexagonales (offset-column)
│
├── model/                           # Modèle de données (MVC)
│   ├── Board.java                   # Plateau hexagonal
│   ├── Cell.java                    # Case hexagonale
│   ├── GamePhase.java               # Phases du tour
│   ├── GameState.java               # État global de la partie
│   ├── Player.java                  # Joueur
│   ├── PlayerColor.java             # Couleurs disponibles
│   │
│   ├── pion/
│   │   ├── Explorer.java            # Pion explorateur
│   │   ├── ExplorerState.java       # État d'un explorateur
│   │   ├── Boat.java                # Pion bateau
│   │   ├── Creature.java            # Pion créature marine
│   │   └── CreatureType.java        # Types de créatures
│   │
│   └── tile/
│       ├── TerrainTile.java         # Tuile de terrain
│       ├── TerrainType.java         # Types de terrain
│       └── TileAction.java          # Actions cachées des tuiles
│
├── controller/
│   ├── GameController.java          # Logique de jeu, validation des actions
│   └── ActionResult.java           # Résultat d'une action (succès/erreur/effets)
│
└── view/                            # Interface graphique Swing
    ├── GameWindow.java              # Fenêtre principale
    ├── SetupDialog.java             # Configuration de la partie
    ├── ScoreDialog.java             # Affichage des scores en fin de partie
    ├── HelpDialog.java              # Aide / règles du jeu
    │
    └── component/
        ├── BoardPanel.java          # Rendu hexagonal du plateau
        ├── HexRenderer.java         # Calculs géométriques hexagonaux
        ├── PlayerInfoPanel.java     # Panneau latéral (joueur courant)
        └── ActionBarPanel.java      # Barre d'actions en bas
```

---

## Architecture MVC

Le projet suit le patron **Modèle-Vue-Contrôleur** :

| Couche       | Rôle |
|--------------|------|
| **Modèle**   | `GameState`, `Board`, `Cell`, `Player`, pions, tuiles — données pures, aucune dépendance vers Swing |
| **Vue**      | `GameWindow`, `BoardPanel`, panneaux — rendu graphique, aucune logique de jeu |
| **Contrôleur** | `GameController` — valide et applique les actions, renvoie un `ActionResult` |

---

## Règles implémentées

### Plateau
- Grille hexagonale 13×9 (offset-column, flat-top)
- 4 plages de coin (zones de sauvetage)
- Zone centrale île avec 40 tuiles terrain

### Tuiles terrain
- **16 Plage** · **16 Forêt** · **8 Montagne**
- Ordre de retrait obligatoire : Plage → Forêt → Montagne
- Chaque tuile doit être adjacente à la mer pour être retirable

### Actions immédiates (contour vert)
| Action | Effet |
|--------|-------|
| Requin | Apparition + nageurs tués |
| Baleine | Apparition + effets sur bateaux |
| Bateau | Apparition + nageurs embarquent |
| Tourbillon | Tout retiré de la zone |
| Volcan | Fin de partie immédiate |

### Tuiles conservées (contour rouge)
| Action | Utilisation |
|--------|-------------|
| Dauphin | Nageur +3 cases (début de tour) |
| Vent | Bateau +3 cases (début de tour) |
| Déplacer créature | Téléporte une créature |
| Chasser requin/baleine | Retire la créature (défense) |

### Créatures marines
| Créature | Déplacement | Effet |
|----------|-------------|-------|
| Serpent de mer 🐍 | 1 case | Coule bateaux chargés, tue nageurs |
| Requin 🦈 | 1–2 cases | Tue les nageurs |
| Baleine 🐋 | 1–3 cases | Chavire les bateaux chargés |

### Tour de jeu
1. Jouer une tuile conservée *(optionnel)*
2. 3 déplacements au total (explorateurs + bateaux)
3. Retirer une tuile *(obligatoire)*
4. Lancer le dé → déplacer une créature

### Score
Somme des valeurs de trésor des explorateurs sauvés (1–6 par pion).  
Égalité départagée par le nombre d'explorateurs sauvés.

---

## Choix de conception notables

### HexCoord et voisinage
La classe `HexCoord` encapsule les coordonnées offset-column et calcule
les 6 voisins en tenant compte de la parité de colonne. La conversion
en coordonnées cubiques sert au calcul de distance.

### ActionResult
Toutes les actions du contrôleur retournent un `ActionResult` contenant
le statut (succès/erreur), un message principal, et une liste d'effets
secondaires. Cela permet à la vue de les afficher sans connaître la logique.

### Pool de tuiles
Les 40 tuiles sont créées dans `GameState.buildTilePool()` selon la
distribution officielle (voir sujet), puis mélangées et posées sur la
zone île. Chaque tuile est immuable après création.

### Contrôle des bateaux
Un bateau est contrôlé par le(s) joueur(s) ayant le plus de passagers
à bord. En cas d'égalité, chaque joueur à égalité le contrôle.
La méthode `Boat.getControllers()` calcule cela dynamiquement.
