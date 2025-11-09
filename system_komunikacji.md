# Specyfikacja Modułu: System Komunikacji (Czat)

## Wersja: 1.5 (stan na 2025-11-09)

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
*   **Funkcjonalność:** Kliknięcie przycisku `+` otwiera dialog (`NewChatDialog`), który wyświetla listę znajomych użytkownika. Wybranie znajomego z listy inicjuje proces tworzenia (lub pobierania istniejącego) czatu i nawiguuje do ekranu rozmowy.

### 2.3. Ekran Listy Konwersacji (`ChatListScreen`)
*   **Cel:** Wyświetlenie wszystkich aktywnych czatów użytkownika.
*   **UI:** Ekran zawiera `LazyColumn` z listą konwersacji, posortowaną od najnowszej do najstarszej. Każdy element listy (`ChatListItem`) pokazuje:
    *   Nazwę wyświetlaną (`username`) rozmówcy.
    *   Fragment ostatniej wiadomości w tej konwersacji.
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
    *   **`ChatListViewModel`:**
        *   Przechowuje stan `uiState` (`StateFlow<Resource<List<ChatWithUserDetails>>>`) dla listy czatów.
        *   Pobiera czaty z `ChatRepository` i łączy je z danymi użytkowników (nazwa, avatar) z `UserRepository`.
        *   Obsługuje logikę tworzenia nowego czatu po wybraniu użytkownika w dialogu.
        *   Zarządza nawigacją do ekranu konwersacji za pomocą `Channel` dla zdarzeń jednorazowych.
    *   **`ConversationViewModel`:**
        *   Otrzymuje `chatId` jako argument nawigacji (`SavedStateHandle`).
        *   Przechowuje stan `messagesState` dla listy wiadomości.
        *   Pobiera wiadomości z `ChatRepository`.
        *   Przechowuje stan pola tekstowego i obsługuje logikę wysyłania nowej wiadomości.

3.  **Warstwa Interfejsu Użytkownika (UI Layer):**
    *   **`ChatListScreen`:** Obserwuje (`collectAsState`) `uiState` z `ChatListViewModel` oraz `friendsState` z `CommunityViewModel`. Wyświetla listę czatów lub stany ładowania/błędu. Obsługuje kliknięcie FAB, pokazując `NewChatDialog`.
    *   **`ConversationScreen`:** Obserwuje `messagesState` z `ConversationViewModel` i wyświetla listę wiadomości w `LazyColumn`.

---

## 5. Implementacja i Rozwiązane Problemy

### 5.1. Ujednolicenie Obsługi Stanu
- **Problem:** W projekcie istniały dwie oddzielne klasy (`Resource` i `Response`) do tego samego celu.
- **Rozwiązanie:** Ujednolicono całą aplikację, aby korzystała z jednej, spójnej klasy `sealed class Resource<out T>`, co wyeliminowało konflikty typów i uprościło kod.

### 5.2. Konfiguracja Firebase

Podczas implementacji napotkano dwa kluczowe problemy:

- **Problem 1: `PERMISSION_DENIED`**
  - **Objaw:** Aplikacja nie mogła pobrać listy czatów, zwracając błąd o braku uprawnień.
  - **Przyczyna:** Domyślne reguły bezpieczeństwa Firestore blokowały dostęp do kolekcji `chats`.
  - **Rozwiązanie:** Zaktualizowano reguły bezpieczeństwa, dodając sekcję, która pozwala zalogowanemu użytkownikowi na odczyt i zapis w dokumencie czatu, **jeśli jego `uid` znajduje się na liście `participants` tego czatu**.
    ```
    match /chats/{chatId} {
      allow read, write: if request.auth != null && request.auth.uid in resource.data.participants;
      // ...
    }
    ```

- **Problem 2: `FAILED_PRECONDITION`**
  - **Objaw:** Po naprawieniu uprawnień, aplikacja nadal zwracała błąd, tym razem informujący o wymaganym indeksie.
  - **Przyczyna:** Zapytanie do Firestore było złożone - jednocześnie filtrowało po polu `participants` i sortowało po `lastMessageTimestamp`. Firestore wymaga do tego "indeksu złożonego" (composite index).
  - **Rozwiązanie:** Wykorzystano link wygenerowany w komunikacie błędu w Logcat. Otwarcie linku przeniosło do konsoli Firebase z automatycznie wypełnionym formularzem. Po kliknięciu "Create Index" i odczekaniu kilku minut na jego zbudowanie, problem został rozwiązany.
