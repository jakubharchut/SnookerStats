# Specyfikacja Modułu: System Komunikacji (Czat)

## Wersja: 1.0 (stan na 2024-07-26)

---

## 1. Cel Główny

Celem tego modułu jest stworzenie w pełni funkcjonalnego, prywatnego systemu wiadomości (czatu) między użytkownikami aplikacji. Ma to na celu wzmocnienie aspektu społecznościowego, ułatwienie umawiania się na mecze sparingowe oraz budowanie relacji między graczami.

---

## 2. Architektura UI i Nawigacja

System komunikacji będzie zintegrowany z głównym interfejsem aplikacji w sposób intuicyjny, zapewniając łatwy dostęp z wielu miejsc.

### 2.1. Główny Punkt Dostępu (`TopAppBar`)
*   **Lokalizacja:** W głównym `TopAppBar` aplikacji (`MainScreen.kt`), obok ikony profilu, zostanie dodana ikona **koperty** lub **dymka czatu**.
*   **Funkcjonalność:** Kliknięcie tej ikony przeniesie użytkownika bezpośrednio do głównego ekranu z listą wszystkich jego konwersacji (`ChatListScreen`).

### 2.2. Inicjowanie Rozmowy (Kontekstowe)
*   **Lokalizacja:** Możliwość rozpoczęcia nowej rozmowy będzie dostępna z poziomu modułu społeczności:
    *   Na liście **znajomych** (`FriendsTab` w `CommunityScreen`).
    *   Na publicznym **profilu innego użytkownika** (`ProfileScreen` oglądany dla innego gracza).
*   **Funkcjonalność:** Obok nazwy użytkownika znajdzie się przycisk lub ikona "Napisz wiadomość". Jego kliknięcie przeniesie użytkownika bezpośrednio do ekranu rozmowy z wybraną osobą (`ConversationScreen`).

### 2.3. Ekran Listy Konwersacji (`ChatListScreen`)
*   **Cel:** Wyświetlenie wszystkich aktywnych czatów użytkownika.
*   **UI:** Ekran będzie zawierał `LazyColumn` z listą konwersacji, posortowaną od najnowszej do najstarszej. Każdy element listy będzie pokazywał:
    *   Nazwę wyświetlaną (`username`) rozmówcy.
    *   Fragment ostatniej wiadomości w tej konwersacji.
    *   Datę lub godzinę ostatniej wiadomości.
    *   Wskaźnik nieprzeczytanych wiadomości.
*   **Interakcja:** Kliknięcie w dowolną konwersację przeniesie użytkownika do `ConversationScreen`.

### 2.4. Ekran Rozmowy (`ConversationScreen`)
*   **Cel:** Prowadzenie rozmowy z jednym, wybranym użytkownikiem.
*   **UI:**
    *   `TopAppBar` z nazwą wyświetlaną rozmówcy.
    *   `LazyColumn` wyświetlający historię wiadomości w formie "dymków czatu" (inne tło dla wiadomości wysłanych i otrzymanych).
    *   Na dole ekranu, `OutlinedTextField` do wpisywania nowej wiadomości oraz przycisk "Wyślij".

---

## 3. Model Danych (Firestore)

Logika czatu będzie oparta o dedykowaną strukturę w bazie danych Cloud Firestore, zoptymalizowaną pod kątem wydajności i skalowalności.

*   **Kolekcja główna: `chats`**
    *   Każdy dokument w tej kolekcji będzie reprezentował jedną, unikalną konwersację między dwoma użytkownikami.
    *   **ID Dokumentu:** Aby zapewnić unikalność i łatwość wyszukiwania, ID dokumentu będzie konkatenacją `uid` obu użytkowników, posortowanych alfabetycznie (np. `uidA_uidB`).
    *   **Pola Dokumentu:**
        *   `participants: List<String>` - Lista `uid` dwóch uczestników czatu.
        *   `lastMessage: String` - Treść ostatniej wiadomości (do wyświetlania na liście konwersacji).
        *   `lastMessageTimestamp: Timestamp` - Czas ostatniej wiadomości (do sortowania).
*   **Subkolekcja: `messages`**
    *   Wewnątrz każdego dokumentu czatu (`chats/{chatId}`) będzie istniała subkolekcja `messages`.
    *   Każdy dokument w tej subkolekcji to pojedyncza wiadomość.
    *   **Pola Dokumentu Wiadomości:**
        *   `senderId: String` - `uid` użytkownika, który wysłał wiadomość.
        *   `text: String` - Treść wiadomości.
        *   `timestamp: Timestamp` - Dokładny czas wysłania wiadomości.

---

## 4. Plan Implementacji

1.  **Etap 1: Fundamenty**
    *   Stworzenie modeli danych (`data class Chat`, `data class Message`).
    *   Stworzenie `ChatRepository` (interfejs i implementacja) z funkcjami do:
        *   Pobierania listy konwersacji użytkownika (`getChats`).
        *   Pobierania wiadomości z konkretnej konwersacji (`getMessages`).
        *   Wysyłania nowej wiadomości (`sendMessage`).
    *   Stworzenie `ChatViewModel` do zarządzania stanem.

2.  **Etap 2: Implementacja UI**
    *   Dodanie ikony wiadomości do `TopAppBar` w `MainScreen.kt`.
    *   Zbudowanie UI dla `ChatListScreen` i `ConversationScreen`.
    *   Podłączenie ekranów do `ChatViewModel`.

3.  **Etap 3: Integracja**
    *   Dodanie przycisków "Napisz wiadomość" w module społeczności.
    *   Zaimplementowanie nawigacji do odpowiednich ekranów czatu.

4.  **Etap 4: Funkcje Zaawansowane (w przyszłości)**
    *   Implementacja nasłuchiwania na zmiany w czasie rzeczywistym za pomocą `snapshotFlow` z Firestore.
    *   Dodanie logiki statusu "przeczytane/nieprzeczytane".
    *   Integracja z Firebase Cloud Messaging w celu wysyłania powiadomień push o nowych wiadomościach.
