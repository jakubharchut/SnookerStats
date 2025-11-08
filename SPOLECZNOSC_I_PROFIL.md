# Specyfikacja Modułu: Społeczność i Profil

## Wersja: 1.3 (stan na 2024-07-30)

---

## 1. Cel Główny

Celem tego modułu jest stworzenie w pełni funkcjonalnego systemu społecznościowego, który umożliwi użytkownikom interakcję, budowanie sieci kontaktów (znajomych) oraz zarządzanie swoim publicznym profilem. Moduł ten jest kluczowy dla budowania zaangażowania i umożliwienia rywalizacji między graczami.

---

## 2. Model Danych (User)

Centralnym modelem danych dla tego modułu jest klasa `User`. Będzie ona przechowywać wszystkie informacje o profilu i relacjach społecznościowych użytkownika.

**Definicja `data class User`:**
*   `uid: String` - (Klucz główny) Unikalny ID z Firebase Authentication.
*   `username: String` - **(Obowiązkowe)** Publiczna, unikalna nazwa wyświetlana.
*   `username_lowercase: String` - Wersja `username` z małymi literami, do wyszukiwania.
*   `email: String` - Adres e-mail (niepubliczny).
*   `firstName: String` - **(Obowiązkowe)** Imię użytkownika.
*   `firstName_lowercase: String` - Wersja `firstName` z małymi literami, do wyszukiwania.
*   `lastName: String` - **(Obowiązkowe)** Nazwisko użytkownika.
*   `lastName_lowercase: String` - Wersja `lastName` z małymi literami, do wyszukiwania.
*   `isPublicProfile: Boolean` - Flaga prywatności. Jeśli `true`, profil jest publiczny. Domyślnie `true`.
*   `club: String?` - **(Opcjonalne)** Nazwa klubu, do którego należy gracz.
*   `profileImageUrl: String?` - **(Opcjonalne)** URL do zdjęcia profilowego.
*   `friends: List<String>` - Lista `uid` użytkowników, którzy są znajomymi.
*   `friendRequestsSent: List<String>` - Lista `uid` użytkowników, do których wysłano zaproszenie.
*   `friendRequestsReceived: List<String>` - Lista `uid` użytkowników, od których otrzymano zaproszenie.

---

## 3. Plan Implementacji Funkcjonalności

### Krok 1: Onboarding - Uzupełnienie Profilu po Rejestracji

**Cel:** Zapewnienie, że każdy użytkownik ma kompletny profil podstawowy.

1.  **Wykrywanie Nowego Użytkownika:**
    *   Po pomyślnym zalogowaniu, aplikacja (w `AuthViewModel`) pobierze dane użytkownika z Firestore.
    *   Sprawdzi, czy pole `username` jest puste.
2.  **Przekierowanie do Formularza:**
    *   Jeśli `username` jest pusty, użytkownik zostanie przekierowany do nowego ekranu `SetupProfileScreen`.
    *   Jeśli `username` nie jest pusty, użytkownik zostanie przekierowany do głównego ekranu aplikacji (`MainScreen`).
3.  **Ekran `SetupProfileScreen`:**
    *   Będzie zawierał formularz z polami: `username`, `firstName`, `lastName` - **wszystkie pola są obowiązkowe**.
    *   **Walidacja:** Pole `username` nie może zawierać spacji. Walidacja odbywa się w czasie rzeczywistym, a przycisk zapisu jest nieaktywny, dopóki wszystkie pola nie są poprawnie wypełnione.
    *   Zawiera przełącznik `Switch` do ustawienia profilu jako publiczny/prywatny.
4.  **Logika Zapisu:**
    *   Po kliknięciu przycisku, dane zostaną zwalidowane (czy `username` nie jest pusty i czy jest unikalny).
    *   Aplikacja zaktualizuje dokument użytkownika w Firestore o nowe dane, w tym o wersje `_lowercase` dla pól tekstowych.
    *   Po pomyślnym zapisie, użytkownik zostanie przekierowany do `MainScreen`.

### Krok 2: Kontekstowe Wymaganie Danych (Turnieje i Kluby)

**Cel:** Wdrożenie strategii "pytaj tylko, gdy to konieczne".

*   **Implementacja:** W przyszłości, podczas implementacji modułu turniejów lub klubów, przed dołączeniem do turnieju lub klubu, aplikacja sprawdzi, czy pola `firstName` i `lastName` w profilu użytkownika są uzupełnione.
*   **Dialog:** Jeśli pola te są puste, zostanie wyświetlony dialog z prośbą o ich uzupełnienie, informując użytkownika, że są one wymagane do udziału w oficjalnych rozgrywkach.

### Krok 3: Ekran Społeczności (`CommunityScreen`)

**Cel:** Stworzenie centralnego miejsca do zarządzania interakcjami społecznymi.

Zaimplementowano strukturę z trzema zakładkami: "Szukaj", "Znajomi", "Zaproszenia".

1.  **Wyszukiwarka Graczy (zakładka "Szukaj"):**
    *   Pole tekstowe do wpisywania frazy.
    *   **Wyszukiwanie odbywa się "na żywo"** (z 500ms opóźnieniem) po wpisaniu co najmniej 3 znaków.
    *   Mechanizm przeszukuje pola `username`, `firstName` i `lastName`, ignorując wielkość liter.
    *   Wyniki wyświetlane są w `LazyColumn`.
    *   Kliknięcie na wynik nawiguje do ekranu profilu danego użytkownika (`UserProfileScreen`).
2.  **Lista Otrzymanych Zaproszeń:**
    *   Sekcja wyświetlająca listę użytkowników, którzy wysłali nam zaproszenie.
    *   Przy każdym zaproszeniu będą przyciski "Akceptuj" i "Odrzuć".
3.  **Lista Znajomych:**
    *   Sekcja wyświetlająca listę naszych aktualnych znajomych.
    *   Każdy element na liście będzie klikalny, prowadząc do profilu gracza.

### Krok 4: Implementacja Logiki Systemu Znajomych (`CommunityRepository`)

**Cel:** Stworzenie logiki backendowej do zarządzania relacjami.

1.  **Wysyłanie Zaproszenia (`sendFriendRequest`):**
    *   Akcja wykonywana po kliknięciu "Dodaj do znajomych".
    *   Aktualizuje dwa dokumenty w Firestore:
        *   Dodaje ID odbiorcy do `friendRequestsSent` u wysyłającego.
        *   Dodaje ID wysyłającego do `friendRequestsReceived` u odbiorcy.
2.  **Akceptowanie Zaproszenia (`acceptFriendRequest`):**
    *   Akcja wykonywana po kliknięciu "Akceptuj".
    *   Wykonuje transakcję w Firestore, która:
        *   Dodaje ID obu użytkowników do ich wzajemnych list `friends`.
        *   Usuwa ID z list `friendRequestsSent` i `friendRequestsReceived`.
3.  **Odrzucanie Zaproszenia (`rejectFriendRequest`):**
    *   Akcja wykonywana po kliknięciu "Odrzuć".
    *   Usuwa ID z list `friendRequestsSent` i `friendRequestsReceived`.
4.  **Usuwanie Znajomego (`removeFriend`):**
    *   Akcja dostępna na profilu znajomego.
    *   Usuwa ID obu użytkowników z ich wzajemnych list `friends`.

### Krok 5: Ekran Profilu Użytkownika (`UserProfileScreen`)

**Cel:** Stworzenie publicznej wizytówki gracza oraz miejsca do edycji własnych danych.

1.  **Logika Widoczności:**
    *   Pełne dane profilu (statystyki itp.) są widoczne, jeśli:
        *   `isPublicProfile` ma wartość `true`.
        *   **LUB** użytkownik przeglądający profil jest na liście `friends` użytkownika, którego profil ogląda.
        *   **LUB** użytkownik ogląda swój własny profil.
    *   W przeciwnym wypadku wyświetlane są tylko podstawowe dane (nazwa użytkownika) i komunikat o prywatności wraz z przyciskiem "Dodaj do znajomych".
2.  **Widok Własnego Profilu:**
    *   Wyświetla te same dane, co widok publiczny.
    *   Dodatkowo zawiera przycisk "Edytuj profil", który pozwoli na zmianę `username`, `firstName`, `lastName`, `club`, `profileImageUrl` oraz flagi `isPublicProfile`.

---

## 4. Wygląd i Interfejs Użytkownika (UI/UX) Modułu Społeczność

### 4.1. Ekran Społeczność - Styl Menu Zakładek

Ekran Społeczności (`CommunityScreen`) wykorzystuje górny pasek zakładek (`TabRow`) do organizacji treści.

*   **Obecny stan:** Ekran `CommunityScreen` ma zaimplementowaną nawigację opartą o `TabRow`.
*   **Komponent:** `TabRow` z `Jetpack Compose Material 3`.
*   **Lokalizacja:** Umieszczony bezpośrednio pod `TopAppBar`.
*   **Zakładki:** Trzy główne zakładki:
    1.  **"Szukaj"**: Przeznaczona do wyszukiwania innych użytkowników.
    2.  **"Znajomi"**: Wyświetla listę aktualnych znajomych użytkownika.
    3.  **"Zaproszenia"**: Służy do zarządzania otrzymanymi i wysłanymi zaproszeniami.
*   **Wizualizacja:** Zakładki będą wyraźnie oddzielone wizualnie, a aktywna zakładka zostanie podkreślona. Nawigacja między zakładkami odbywać się będzie poprzez kliknięcie. W przyszłości można rozważyć dodanie `HorizontalPager` do obsługi przesuwania palcem (swiping).
*   **Interakcja:** Kliknięcie w nazwę zakładki powoduje natychmiastowe przejście do odpowiedniej treści.
*   **Przykładowy Widok:** Zgodny ze zrzutem ekranu, który był punktem odniesienia dla tego opisu.
