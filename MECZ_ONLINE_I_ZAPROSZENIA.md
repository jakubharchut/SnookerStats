# Specyfikacja Funkcjonalności: Mecz Online (Tryb Obserwatora)

## Wersja: 1.1 (stan na 2025-11-12)

---

## 1. Cel Główny

Celem tej funkcjonalności jest umożliwienie drugiemu graczowi otrzymania powiadomienia o rozpoczętym meczu oraz dołączenia do niego w trybie "tylko do odczytu" (obserwatora). Model ten zakłada, że tylko jeden użytkownik (inicjator meczu) aktywnie wprowadza wyniki.

---

## 2. Architektura i Przepływ

### 2.1. Rozpoczęcie Meczu
1.  **Inicjator (Gracz 1):** Na ekranie `MatchSetupScreen` wybiera przeciwnika i klika "Rozpocznij Mecz".
2.  **System:**
    - W bazie danych Firestore tworzony jest nowy dokument meczu ze statusem `IN_PROGRESS`.
    - Inicjator jest natychmiast przenoszony do ekranu `ScoringScreen` i może rozpocząć wprowadzanie wyników.

### 2.2. Powiadomienie dla Przeciwnika (Gracz 2)
1.  **Cloud Function (`sendNewMatchNotification`):**
    - Utworzenie nowego dokumentu w kolekcji `matches` automatycznie uruchamia funkcję w chmurze.
    - Funkcja sprawdza, czy mecz jest rozgrywany przeciwko zarejestrowanemu użytkownikowi (a nie gościowi lub solo).
    - Pobiera token FCM Gracza 2 i wysyła do niego powiadomienie push z informacją o rozpoczęciu meczu.

2.  **Kod Cloud Function (do wdrożenia w `index.js`):**
    ```javascript
    const functions = require("firebase-functions");
    const admin = require("firebase-admin");
    
    // Inicjalizacja admina, jeśli jeszcze nie istnieje
    // admin.initializeApp();

    exports.sendNewMatchNotification = functions.firestore
        .document("matches/{matchId}")
        .onCreate(async (snap, context) => {
            const matchData = snap.data();

            if (!matchData.player2Id || matchData.player2Id.startsWith("guest_")) {
                console.log("Mecz solo lub z gościem, nie wysyłamy powiadomienia.");
                return null;
            }

            const player1Id = matchData.player1Id;
            const player2Id = matchData.player2Id;

            const player1Doc = await admin.firestore().collection("users").doc(player1Id).get();
            const player2Doc = await admin.firestore().collection("users").doc(player2Id).get();

            if (!player1Doc.exists || !player2Doc.exists) {
                console.log("Nie znaleziono jednego z graczy.");
                return null;
            }

            const player1Data = player1Doc.data();
            const player2Data = player2Doc.data();
            const recipientToken = player2Data.fcmToken;

            if (!recipientToken) {
                console.log("Odbiorca nie ma tokenu FCM.");
                return null;
            }

            const payload = {
                notification: {
                    title: "Rozpoczęto nowy mecz!",
                    body: `${player1Data.firstName} ${player1Data.lastName} zaprasza Cię do oglądania waszego meczu.`,
                },
                data: {
                  matchId: context.params.matchId,
                  screen: "scoring" 
                }
            };

            console.log(`Wysyłanie powiadomienia do ${player2Id}...`);
            try {
                const message = {
                  token: recipientToken,
                  notification: payload.notification,
                  data: payload.data
                };
                await admin.messaging().send(message);
                console.log("Powiadomienie wysłane pomyślnie.");
            } catch (error) {
                console.error("Błąd podczas wysyłania powiadomienia:", error);
            }

            return null;
        });
    ```

### 2.3. Dołączenie jako Obserwator
1.  **Przeciwnik (Gracz 2):** Po otrzymaniu powiadomienia, klika w nie. Aplikacja otwiera się i (w przyszłości) automatycznie przechodzi do trwającego meczu.
2.  **Alternatywnie:** Gracz 2 może wejść w zakładkę "Graj", gdzie (dzięki funkcji "Powrót do meczu") zobaczy trwający mecz i zostanie do niego automatycznie przekierowany.
3.  **Tryb "Tylko do odczytu":**
    - Ekran `ScoringScreen` musi zostać zmodyfikowany.
    - Musi sprawdzać, czy zalogowany użytkownik jest inicjatorem meczu (Graczem 1).
    - Jeśli **nie jest**, wszystkie przyciski do wprowadzania punktów (`onBallClicked`, `onFoulClicked` itp.) muszą być **wyłączone (disabled)**.
    - Ekran będzie się aktualizował w czasie rzeczywistym dzięki nasłuchiwaniu na zmiany w dokumencie meczu w Firestore, ale interakcja będzie zablokowana.

---

## 3. Zadania do Wykonania

- [ ] Wdrożenie powyższej funkcji `sendNewMatchNotification` do środowiska Firebase Cloud Functions.
- [ ] Modyfikacja `ScoringViewModel` lub `ScoringScreen`, aby wprowadzić logikę trybu "tylko do odczytu" w zależności od ID zalogowanego użytkownika.
- [ ] (Opcjonalnie) Implementacja obsługi "głębokich linków" (deep linking), aby kliknięcie w powiadomienie przenosiło bezpośrednio do ekranu meczu.
