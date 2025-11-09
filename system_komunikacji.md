# Specyfikacja Modułu: System Komunikacji (Czat)

## Wersja: 1.6 (stan na 2025-11-09)

---

## 1. Cel Główny

Celem tego modułu jest stworzenie w pełni funkcjonalnego, prywatnego systemu wiadomości (czatu) między użytkownikami aplikacji. Ma to na celu wzmocnienie aspektu społecznościowego, ułatwienie umawiania się na mecze sparingowe oraz budowanie relacji między graczami.

---

## 2. Architektura UI i Nawigacja

System komunikacji jest zintegrowany z głównym interfejsem aplikacji w sposób intuicyjny, zapewniając łatwy dostęp z wielu miejsc.

### 2.1. Główny Punkt Dostępu (`TopAppBar`)
*   **Lokalizacja:** W głównym `TopAppBar` aplikacji (`MainScreen.kt`), obok ikony profilu, znajduje się ikona wiadomości (`Icons.Filled.Forum`).
*   **Funkcjonalność:** Kliknięcie tej ikony przenosi użytkownika bezpośrednio do głównego ekranu z listą wszystkich jego konwersacji (`ChatListScreen`).

### 2.2. Inicjowanie Nowej Rozmowy
*   **Lokalizacja:** W `ChatListScreen` znajduje się pływający przycisk akcji (FAB) z ikoną `+`.
*   **Funkcjonalność:** Kliknięcie przycisku `+` otwiera wysuwany od góry panel (`TopSheet`), który wyświetla listę znajomych użytkownika. Wybranie znajomego z listy inicjuje proces tworzenia (lub pobierania istniejącego) czatu i nawiguje do ekranu rozmowy.

### 2.3. Ekran Listy Konwersacji (`ChatListScreen`)
*   **Cel:** Wyświetlenie wszystkich aktywnych czatów użytkownika.
*   **UI:** Ekran zawiera `LazyColumn` z listą konwersacji, posortowaną od najnowszej do najstarszej. Każdy element listy (`ChatListItem`) pokazuje:
    *   Nazwę wyświetlaną (`username`) rozmówcy.
    *   Fragment ostatniej wiadomości w tej konwersacji (lub "..." jeśli jest pusta).
*   **Interakcja:** Kliknięcie w dowolną konwersację przenosi użytkownika do `ConversationScreen`.

### 2.4. Ekran Rozmowy (`ConversationScreen`)
*   **Cel:** Prowadzenie rozmowy z jednym, wybranym użytkownikiem.
*   **UI:**
    *   `TopAppBar` z nazwą wyświetlaną rozmówcy i przyciskiem powrotu.
    *   `LazyColumn` wyświetlający historię wiadomości w formie "dymków czatu".
    *   Na dole ekranu, `OutlinedTextField` do wpisywania nowej wiadomości oraz przycisk "Wyślij".

---

## 3. Model Danych (Firestore)

Logika czatu jest oparta o dedykowaną strukturę w bazie danych Cloud Firestore, zoptymalizowaną pod kątem wydajności i skalowalności.

*   **Kolekcja główna: `chats`**
    *   Każdy dokument w tej kolekcji reprezentuje jedną, unikalną konwersację między dwoma użytkownikami.
    *   **ID Dokumentu:** Aby zapewnić unikalność i łatwość wyszukiwania, ID dokumentu jest konkatenacją `uid` obu użytkowników, posortowanych alfabetycznie i połączonych znakiem `_` (np. `uidA_uidB`).
    *   **Pola Dokumentu:**
        *   `participants: List<String>` - Lista `uid` dwóch uczestników czatu.
        *   `lastMessage: String?` - Treść ostatniej wiadomości (do wyświetlania na liście konwersacji).
        *   `lastMessageTimestamp: Timestamp?` - Czas ostatniej wiadomości (do sortowania).
*   **Subkolekcja: `messages`**
    *   Wewnątrz każdego dokumentu czatu (`chats/{chatId}`) istnieje subkolekcja `messages`.
    *   Każdy dokument w tej subkolekcji to pojedyncza wiadomość.
    *   **Pola Dokumentu Wiadomości:**
        *   `senderId: String` - `uid` użytkownika, który wysłał wiadomość.
        *   `text: String` - Treść wiadomości.
        *   `timestamp: Timestamp` - Dokładny czas wysłania wiadomości.

---

## 4. Przepływ Danych (Architektura Kodu)

Architektura modułu opiera się na wzorcu MVVM i jest podzielona na trzy główne warstwy:

1.  **Warstwa Danych (Data Layer):**
    *   **`ChatRepository` / `ChatRepositoryImpl`:** Odpowiada za całą komunikację z Firestore.
    *   `getChats()` i `getMessages()`: Używają `callbackFlow`, aby nasłuchiwać na zmiany w Firestore w czasie rzeczywistym (`addSnapshotListener`) i emitować je jako `Flow<Resource<List<...>>>`. To zapewnia automatyczne odświeżanie UI.
    *   `sendMessage()`: Zapisuje nową wiadomość i aktualizuje pola `lastMessage` i `lastMessageTimestamp` w głównym dokumencie czatu w ramach jednej transakcji (`batch write`).
    *   `createOrGetChat()`: Sprawdza, czy czat między dwoma użytkownikami już istnieje. Jeśli tak, zwraca jego ID. Jeśli nie, tworzy nowy dokument czatu i zwraca jego ID.

2.  **Warstwa Logiki Biznesowej (ViewModel Layer):**
    *   **`ChatListViewModel`:** Zarządza logiką ekranu listy czatów, w tym tworzeniem nowych konwersacji.
    *   **`ConversationViewModel`:** Zarządza logiką pojedynczego ekranu rozmowy.
    *   **`CommunityViewModel`:** Zawiera również logikę do inicjowania czatu z poziomu listy znajomych.

3.  **Warstwa Interfejsu Użytkownika (UI Layer):**
    *   **`ChatListScreen`:** Wyświetla listę czatów i panel do tworzenia nowej rozmowy.
    *   **`ConversationScreen`:** Wyświetla widok pojedynczej rozmowy.

---

## 5. Implementacja i Rozwiązane Problemy

### 5.1. Ujednolicenie Obsługi Stanu
- **Problem:** W projekcie istniały dwie oddzielne klasy (`Resource` i `Response`) do tego samego celu.
- **Rozwiązanie:** Ujednolicono całą aplikację, aby korzystała z jednej, spójnej klasy `sealed class Resource<out T>`, co wyeliminowało konflikty typów i uprościło kod.

### 5.2. Konfiguracja Dagger/Hilt
- **Problem:** Aplikacja nie kompilowała się z powodu błędu `DuplicateBindings`. Dwie różne definicje `ChatRepository` istniały w `FirebaseModule` i `RepositoryModule`.
- **Rozwiązanie:** Usunięto przestarzały `RepositoryModule`, centralizując wszystkie definicje repozytoriów w `FirebaseModule`, co rozwiązało konflikt.

### 5.3. Błędy Reguł Bezpieczeństwa Firestore (`PERMISSION_DENIED`)
- **Problem:** Inicjowanie czatu, ładowanie listy czatów oraz usuwanie powiadomień kończyło się błędem `PERMISSION_DENIED`.
- **Przyczyna:** Reguły bezpieczeństwa były zbyt restrykcyjne i nie uwzględniały wszystkich przypadków użycia (np. konieczności sprawdzenia istnienia dokumentu przed jego odczytaniem, tzw. "problem jajka i kurczaka").
- **Rozwiązanie:** Wdrożono tymczasowe, bardzo otwarte reguły na czas developmentu, które pozwalają każdemu zalogowanemu użytkownikowi na pełny dostęp do bazy. To wyeliminowało wszystkie problemy z uprawnieniami.
  ```
  rules_version = '2';
  service cloud.firestore {
    match /databases/{database}/documents {
      match /{document=**} {
        allow read, write: if request.auth != null;
      }
    }
  }
  ```

### 5.4. Crash Aplikacji przy Nawigacji (`IllegalArgumentException`)
- **Problem:** Po naprawieniu reguł bezpieczeństwa, aplikacja zaczęła się wywalać przy próbie przejścia do ekranu rozmowy z błędem `Navigation destination ... cannot be found`.
- **Przyczyna:** W aplikacji istniały dwa kontrolery nawigacji (`NavController`): główny (z `MainActivity`) i wewnętrzny (w `MainScreen`). `ChatListScreen` był wywoływany z wewnętrznym kontrolerem, który nie "widział" globalnej trasy `conversation/{...}`.
- **Rozwiązanie:** Przeprowadzono refaktoryzację `MainScreen.kt`, aby zapewnić, że ekrany wymagające nawigacji "na zewnątrz" (jak `ChatListScreen` czy `CommunityScreen`) otrzymują główny `NavController`, co rozwiązało problem crashy.
