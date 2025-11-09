# Specyfikacja Modułu: System Komunikacji (Czat)

## Wersja: 1.1 (stan na 2024-07-29)

---

## 1. Cel Główny

Celem tego modułu jest stworzenie w pełni funkcjonalnego, prywatnego systemu wiadomości (czatu) między użytkownikami aplikacji. Ma to na celu wzmocnienie aspektu społecznościowego, ułatwienie umawiania się na mecze sparingowe oraz budowanie relacji między graczami.

---

## 2. Architektura UI i Nawigacja

System komunikacji będzie zintegrowany z głównym interfejsem aplikacji w sposób intuicyjny, zapewniając łatwy dostęp z wielu miejsc.

### 2.1. Główny Punkt Dostępu (`TopAppBar`)
*   **Lokalizacja:** W głównym `TopAppBar` aplikacji (`MainScreen.kt`), obok ikony profilu, została dodana ikona **kilku dymków czatu** (`Icons.Filled.Forum`).
*   **Wizualizacja Badge'a:** Ikona wiadomości posiada nałożony badge (czerwone kółko z licznikiem nieprzeczytanych wiadomości), umiejscowiony z przesunięciem `Modifier.offset(x = (-12).dp, y = (-4).dp)`, aby zapewnić jego pełną widoczność i prawidłowe pozycjonowanie względem ikony.
*   **Funkcjonalność:** Kliknięcie tej ikony przeniesie użytkownika bezpośrednio do głównego ekranu z listą wszystkich jego konwersacji (`ChatListScreen`). Ekran `ChatListScreen.kt` został wstępnie utworzony jako placeholder z tekstem "Chat List Screen Content".

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

---
## 5. Diagnostyka i Naprawy (Listopad 2025)

### 5.1. Ujednolicenie Obsługi Stanu (`Resource` vs `Response`)
- **Problem:** W projekcie istniały dwie oddzielne klasy (`Resource` i `Response`) służące do tego samego celu - opakowywania wyników operacji asynchronicznych. Powodowało to konflikty typów i awarie aplikacji, gdy różne moduły (np. Czat i Społeczność) musiały ze sobą współpracować.
- **Rozwiązanie:** Przeprowadzono refaktoryzację całej aplikacji. Stara klasa `Response` została usunięta, a wszystkie repozytoria, ViewModele i ekrany UI zostały zaktualizowane, aby używać wyłącznie nowej, spójnej klasy `sealed class Resource<out T>`.

### 5.2. Rozwiązane Problemy z Konfiguracją Firebase

Podczas implementacji napotkano dwa kluczowe problemy związane z konfiguracją Firebase:

- **Problem 1: `PERMISSION_DENIED`**
  - **Objaw:** Aplikacja nie mogła pobrać listy czatów, zwracając błąd o braku uprawnień.
  - **Przyczyna:** Domyślne reguły bezpieczeństwa Firestore blokowały dostęp do kolekcji `chats`.
  - **Rozwiązanie:** Zaktualizowano reguły bezpieczeństwa, dodając sekcję, która pozwala zalogowanemu użytkownikowi na odczyt i zapis w dokumencie czatu, **jeśli jego `uid` znajduje się na liście `participants` tego czatu**.
    ```
    match /chats/{chatId} {
      allow read, write: if request.auth != null && request.auth.uid in resource.data.participants;
      // ... reguły dla subkolekcji messages ...
    }
    ```

- **Problem 2: `FAILED_PRECONDITION`**
  - **Objaw:** Po naprawieniu uprawnień, aplikacja nadal zwracała błąd, tym razem informujący o wymaganym indeksie.
  - **Przyczyna:** Zapytanie do Firestore było złożone - jednocześnie filtrowało po polu `participants` i sortowało po `lastMessageTimestamp`. Do obsługi takich zapytań Firestore wymaga stworzenia "indeksu złożonego" (composite index).
  - **Rozwiązanie:** Wykorzystano link wygenerowany w komunikacie błędu (w Logcat). Otwarcie linku przeniosło do konsoli Firebase z automatycznie wypełnionym formularzem tworzenia indeksu. Po kliknięciu "Create Index" i odczekaniu kilku minut na jego zbudowanie, problem został rozwiązany.
