# Specyfikacja Projektu: Aplikacja "Snooker Stats"

## Wersja: 1.0 (stan na 2024-07-25)

---

## 1. Wizja i Cel Główny

Stworzenie zaawansowanej, społecznościowej platformy mobilnej dla amatorów snookera. Aplikacja ma służyć nie tylko jako cyfrowy notatnik do zapisywania wyników, ale przede wszystkim jako inteligentne narzędzie do głębokiej analizy statystyk, śledzenia postępów treningowych, rywalizacji z innymi graczami oraz organizacji amatorskich turniejów. Celem jest zbudowanie zaangażowanej społeczności i dostarczenie narzędzia, które realnie pomaga graczom w rozwoju swoich umiejętności.

---

## 2. Architektura i Fundament Technologiczny

### 2.1. Platforma Backendowa: Google Firebase
Aplikacja będzie w pełni oparta o ekosystem Google Firebase, co zapewnia skalowalność, funkcje czasu rzeczywistego i bezpieczeństwo.
*   **Baza Danych:** **Cloud Firestore** jako główne i jedyne źródło prawdy. Będzie przechowywać wszystkie dane użytkowników, mecze, statystyki, turnieje, itp. Jej mechanizmy real-time są kluczowe dla funkcji meczów online.
*   **Uwierzytelnianie:** **Firebase Authentication** do zarządzania kontami użytkowników (rejestracja, logowanie przez e-mail/hasło, dostawców społecznościowych jak Google).
*   **Weryfikacja E-mail:** Po rejestracji, na adres użytkownika automatycznie wysyłany jest link weryfikacyjny. Dostęp do pełnej funkcjonalności aplikacji będzie możliwy dopiero po potwierdzeniu adresu e-mail.
*   **Akceptacja Regulaminu:** Proces rejestracji będzie wymagał od użytkownika aktywnego zaznaczenia zgody na regulamin serwisu. Przycisk rejestracji pozostanie nieaktywny do momentu wyrażenia zgody.
*   **Zapamiętywanie Sesji Logowania:** Aplikacja oferuje opcję "Zapamiętaj mnie" (Checkbox w `LoginScreen`), która pozwala na zapisanie e-maila i hasła w bezpiecznym miejscu na urządzeniu (Encrypted SharedPreferences). Po zaznaczeniu tej opcji i udanym logowaniu, dane uwierzytelniające są zapisywane i automatycznie wypełniane przy kolejnych uruchomieniach aplikacji. Odznaczenie tej opcji usunie wcześniej zapisane dane. Wylogowanie użytkownika nie będzie usuwać zapisanych danych.

### 2.2. Model Danych: "Online-First"
Aplikacja jest projektowana z myślą o stałym dostępie do internetu. Wszystkie operacje zapisu i odczytu danych docelowo kierowane są do chmury, co zapewnia spójność danych na wszystkich urządzeniach użytkownika.

### 2.3. Wsparcie Offline: Lokalna Baza Danych Room
Lokalna baza danych **Room** będzie pełnić rolę **pamięci podręcznej (cache)**, a nie głównego źródła danych.
*   **Cel:** Zapewnienie błyskawicznego działania interfejsu (odczyt z lokalnej kopii), możliwość przeglądania danych bez połączenia z internetem oraz możliwość zapisania meczu w trybie offline, który zostanie automatycznie zsynchronizowany z Firebase po odzyskaniu połączenia.

---

## 3. Kluczowe Funkcjonalności

### 3.1. Zarządzanie Profilem i Społeczność
*   **Stopniowe Wdrażanie (Progressive Onboarding):** Po pierwszej, uproszczonej rejestracji (tylko e-mail i hasło), użytkownik będzie zachęcany do uzupełnienia swojego profilu (nazwa użytkownika, imię, klub) w dedykowanej sekcji ustawień.
*   **Profile Graczy:**
    *   Każdy użytkownik posiada profil z możliwością ustawienia go jako **publiczny** lub **prywatny**.
    *   Profil publiczny działa jak "wizytówka gracza", pokazując jego imię, zdjęcie, przynależność klubową, zdobyte trofea i odznaki oraz kluczowe statystyki.
*   **Wyszukiwarka Graczy:** Dedykowana funkcja pozwalająca na odnalezienie innych użytkowników po ich nazwie lub klubie.
*   **System Sparing Partnerów ("Znajomych"):** Możliwość wysyłania zaproszeń do innych graczy, tworzenia listy znajomych i zarządzania nią.
*   **Kluby:** Funkcjonalność tworzenia i dołączania do grup (klubów), które posiadają własne, wewnętrzne rankingi i statystyki.

### 3.2. Rejestrowanie Meczy
*   **Dwa Tryby Gry:**
    1.  **Mecz Online (Live):** Rozgrywka w czasie rzeczywistym ze sparing partnerem. Obaj gracze korzystają z współdzielonego ekranu do wprowadzania wyników uderzenie po uderzeniu.
    2.  **Mecz Lokalny (Solo):** Możliwość samodzielnego wprowadzenia wyników meczu rozegranego offline.
*   **Kategoryzacja Meczu:** Każdy mecz musi być oznaczony jako **Rankingowy** (liczony do oficjalnych statystyk) lub **Sparingowy** (towarzyski).
*   **Współdzielona Historia:** Wynik meczu automatycznie pojawia się w historii obu graczy. Każdy z nich ma możliwość niezależnego usunięcia meczu ze swojego profilu.

### 3.3. Moduł Turniejów
*   **Tworzenie Turniejów:** Użytkownik (organizator) może stworzyć nowy turniej, definiując jego nazwę, format i datę.
*   **Zarządzanie Uczestnikami:** Organizator może zapraszać uczestników, zarówno **użytkowników aplikacji** z listy znajomych, jak i **graczy gościnnych** (spoza aplikacji) poprzez wpisanie ich imienia.
*   **Automatyczna Drabinka:** Aplikacja automatycznie generuje i wizualizuje drabinkę turniejową (np. w systemie pucharowym).
*   **Aktualizacje na Żywo:** Organizator wprowadza wyniki poszczególnych meczy, a drabinka aktualizuje się w czasie rzeczywistym dla wszystkich uczestników.

### 3.4. Moduł Treningowy
*   **Dedykowana sekcja** z predefiniowanymi ćwiczeniami (np. "Line-up", wbijanie długich bil, trening odstaw).
*   **Zapis Sesji:** Każda sesja treningowa jest zapisywana, a postępy dla danego ćwiczenia są wizualizowane na **wykresie liniowym w czasie**.

### 3.5. Analiza i Statystyki
*   **Głęboka Analiza "Drill-Down":** Umożliwia nawigację od ogólnych statystyk, przez listę konkretnych meczy, aż po szczegółową analizę pojedynczego frejma.
*   **Wizualizacja Gry:** Dla każdego meczu i frejma dostępny będzie **histogram punktowy**, pokazujący przebieg gry uderzenie po uderzeniu.
*   **Zaawansowane Statystyki Brejków:** Aplikacja będzie zliczać i analizować breki w progach (20+, 30+, 50+, 100+).

### 3.6. Rywalizacja i Grywalizacja
*   **Porównania Head-to-Head:** Specjalny ekran do bezpośredniego porównania swoich statystyk z wybranym sparing partnerem.
*   **Rozbudowane Rankingi:** System rankingów z możliwością filtrowania według zasięgu (znajomi, klub, globalnie), konkretnej statystyki oraz okresu czasowego.
*   **Osiągnięcia, Odznaki i Puchary:**
    *   System automatycznie przyznawanych **odznak** za osiąganie kamieni milowych (np. "Pierwszy brejk 100+").
    *   Za wygranie turnieju zwycięzca otrzymuje na swoim profilu **specjalny puchar/trofeum**.

---

## 4. Wygląd i Interfejs Użytkownika (UI/UX)

### 4.1. Motyw Przewodni: "Light Mode First"
Aplikacja będzie oparta o jasny, czysty i profesjonalny wygląd, z opcją dodania trybu ciemnego w przyszłości.
*   **Tło:** Białe lub bardzo jasnoszare.
*   **Tekst:** Ciemnoszary (grafitowy).
*   **Kolor Akcentujący:** Stonowany, elegancki zielony (kolor sukna snookerowego) dla elementów interaktywnych.
*   **Kolor Wyróżniający:** Złoty/żółty dla odznak, trofeów, rekordów i osiągnięć.

### 4.2. Spójna Struktura Ekranów
*   **Wykorzystanie `Scaffold`:** Każdy główny ekran w aplikacji będzie zbudowany w oparciu o komponent `Scaffold` z Jetpack Compose, aby zapewnić spójność.
*   **Górny Pasek Aplikacji (`TopAppBar`):** Zawiera tytuł ekranu i opcjonalne akcje kontekstowe.
*   **Dolny Pasek Nawigacyjny (`BottomNavigationBar`):** Główna nawigacja między kluczowymi sekcjami (np. Dashboard, Graj, Społeczność, Turnieje, Profil).
*   **Obszar Treści:** Centralna część ekranu, w której wyświetlana jest zawartość.

### 4.3. Priorytety Projektowe
*   **Czytelność i Intuicyjność:** Interfejs musi być prosty w obsłudze, nawet podczas stresującego meczu.
*   **Wizualizacja Danych:** Duży nacisk na atrakcyjne i zrozumiałe prezentowanie danych za pomocą wykresów i grafów.
*   **Modularność:** Dzięki `Scaffold`, zmiany w globalnych elementach UI będą łatwe do wprowadzenia bez naruszania struktury poszczególnych ekranów.

---

## 5. Model Monetyzacji

### 5.1. Wersja Darmowa
*   **Dostęp do Pełnej Funkcjonalności:** Użytkownicy mają dostęp do wszystkich funkcji aplikacji, w tym meczów online i turniejów.
*   **Wyświetlanie Reklam:** Wersja darmowa będzie zawierać dyskretne, nieinwazyjne reklamy (np. banery, reklamy pełnoekranowe po zakończeniu meczu).

### 5.2. Wersja Płatna ("Ad-Free")
*   **Jednorazowa Opłata:** Użytkownicy mogą dokonać niewielkiej, jednorazowej opłaty wewnątrz aplikacji.
*   **Usunięcie Reklam:** Zakup ten permanentnie usuwa wszystkie reklamy z aplikacji, zapewniając nieprzerwane doświadczenie.
*   **Odznaka Wspierającego:** Jako podziękowanie, użytkownicy, którzy dokonają zakupu, mogą otrzymać specjalną odznakę na swoim profilu.

---

## 6. Plan Realizacji Projektu (Roadmap)

### Etap 1: Fundamenty i Konfiguracja
- [x] Utworzenie projektu w konsoli Firebase.
- [x] Dodanie zależności do `build.gradle.kts` (Firebase, Hilt, Room, Compose Navigation, etc.).
- [x] Stworzenie bazowej struktury pakietów (`data`, `domain`, `ui`).
- [x] Podstawowa konfiguracja Hilt.

### Etap 2: Uwierzytelnianie Użytkownika
- [x] Zbudowanie UI dla ekranów Logowania i Rejestracji.
- [x] Stworzenie podstawowej nawigacji między ekranami autentykacji.
- [x] Stworzenie `AuthViewModel` do obsługi logiki.
- [x] Podłączenie logiki do Firebase Authentication (Rejestracja i Logowanie).
- [x] Implementacja wysyłania e-maila weryfikacyjnego.
- [x] Zablokowanie dostępu dla niezweryfikowanych użytkowników.
- [x] Stworzenie nawigacji po zalogowaniu (przekierowanie).

### Etap 3: Szkielet UI i Nawigacja Główna
- [x] Implementacja głównego ekranu z `Scaffold`.
- [x] Implementacja `BottomNavigationBar`.
- [x] Stworzenie pustych ekranów dla każdej sekcji.
- [x] Implementacja nawigacji do ekranu profilu oraz akcji wylogowania w TopAppBar.

### Etap 4: Modele Danych i Baza Lokalna
- [ ] Stworzenie klas danych (`Match`, `Tournament`, etc.).
- [ ] Konfiguracja bazy danych Room (Encje, DAO, Database).

### Etap 5: Rdzeń Aplikacji - Zapis Meczu Lokalnego
- [ ] UI ekranu wprowadzania wyniku (shot-by-shot).
- [ ] ViewModel zarządzający stanem meczu.
- [ ] Logika zapisu meczu do Room i Firestore.

### Etap 6: Wyświetlanie Danych
- [ ] Ekran historii meczy.
- [ ] Dashboard z podstawowymi statystykami.

### Etap 7: Funkcje Społecznościowe
- [ ] Wyszukiwarka graczy i profil publiczny.
- [ ] System zaproszeń do znajomych.
- [ ] Ekran porównania statystyk Head-to-Head.

### Etap 8: Mecz Online w Czasie Rzeczywistym
- [ ] Synchronizacja danych przy użyciu listenerów Firestore.
- [ ] System zapraszania do gry online.
- [ ] Obsługa przypadków brzegowych (np. utrata połączenia).

### Etap 9: Moduł Turniejów
- [ ] UI do tworzenia turnieju i zapraszania graczy.
- [ ] Logika generowania drabinki turniejowej.
- [ ] Interfejs do wprowadzania wyników i aktualizacji drabinki.

### Etap 10: Funkcje Zaawansowane i Grywalizacja
- [ ] Implementacja Modułu Treningowego.
- [ ] System przyznawania Odznak, Osiągnięć i Pucharów.
- [ ] Zbudowanie rozbudowanych Rankingów.

### Etap 11: Implementacja Monetyzacji
- [ ] Integracja z Google AdMob i wyświetlanie reklam w wersji darmowej.
- [ ] Implementacja jednorazowego zakupu w aplikacji (In-App Purchase) w celu usunięcia reklam.

### Etap 12: Testowanie, Poprawki i Publikacja
- [ ] Testy jednostkowe i UI.
- [ ] Dopracowanie detali wizualnych i animacji.
- [ ] **Zabezpieczenie reguł Firebase:** Zmiana reguł dostępu do Firestore z trybu testowego na produkcyjny.
- [ ] Publikacja w sklepie Google Play.

---
## 7. Implementacja - Szczegóły Techniczne

### 7.1. Przepływ Rejestracji Użytkownika (`AuthViewModel`)
Logika rejestracji jest w pełni zamknięta w `AuthViewModel` i przebiega według następujących kroków:

1.  **Inicjacja:**
    *   `RegisterScreen` wywołuje publiczną funkcję `registerUser(email, password, confirmPassword)`.
2.  **Walidacja Danych:**
    *   Wewnątrz `ViewModelu`, przed wywołaniem Firebase, przeprowadzana jest walidacja:
        *   Sprawdzenie, czy żadne z pól nie jest puste.
        *   Sprawdzenie, czy `password` i `confirmPassword` są identyczne.
        *   Sprawdzenie, czy `password` ma co najmniej 6 znaków (wymóg Firebase).
        *   Sprawdzenie, czy `email` ma poprawny format.
    *   Jeśli walidacja nie powiedzie się, `ViewModel` wystawi odpowiedni stan błędu (np. `State.Error("Hasła nie są zgodne")`), który UI będzie mogło wyświetlić użytkownikowi.
3.  **Proces Rejestracji:**
    *   Jeśli walidacja przejdzie pomyślnie, `ViewModel` wystawia stan `State.Loading`, aby UI mogło pokazać wskaźnik postępu (np. kółko).
    *   Wywoływana jest funkcja `repo.registerUser(...)`.
4.  **Obsługa Wyniku:**
    *   **Sukces:**
        *   Repozytorium tworzy użytkownika w Auth, wysyła e-mail weryfikacyjny i tworzy dokument w Firestore z pustym polem `username`.
        *   Po pomyślnym zakończeniu operacji w repozytorium, `ViewModel` emituje jednorazowe zdarzenie `NavigationEvent.NavigateToRegistrationSuccess`.
    *   **Błąd:**
        *   Jeśli jakakolwiek operacja się nie powiedzie, `ViewModel` wystawia stan `State.Error` z odpowiednim komunikatem.
5.  **Komunikacja z UI:**
    *   `RegisterScreen` obserwuje stan (`State`) i reaguje na błędy lub stan ładowania.
    *   Nasłuchuje również na zdarzenia `NavigationEvent` i po otrzymaniu `NavigateToRegistrationSuccess`, nawiguje do nowego ekranu `RegistrationSuccessScreen`.

### 7.2. Przepływ Logowania Użytkownika (`AuthViewModel`)
Logika logowania jest w pełni zamknięta w `AuthViewModel` i przebiega według następujących kroków:

1.  **Inicjacja:**
    *   `LoginScreen` wywołuje publiczną funkcję `loginUser(email, password, rememberMe)`. W interfejsie użytkownika znajduje się `Checkbox` "Zapamiętaj mnie", którego stan (`rememberMe`) jest przekazywany do `ViewModelu`.
2.  **Walidacja Danych:**
    *   Wewnątrz `ViewModelu`, przed wywołaniem Firebase, przeprowadzana jest walidacja:
        *   Sprawdzenie, czy żadne z pól `email` i `password` nie jest puste.
    *   Jeśli walidacja nie powiedzie się, `ViewModel` wystawi odpowiedni stan błędu (np. `State.Error("Wszystkie pola muszą być wypełnione.")`), który UI będzie mogło wyświetlić użytkownikowi.
3.  **Proces Logowania:**
    *   Jeśli walidacja przejdzie pomyślnie, `ViewModel` wystawia stan `State.Loading`, aby UI mogło pokazać wskaźnik postępu (np. kółko).
    *   Wywoływana jest funkcja `repo.loginUser(...)`.
4.  **Obsługa Wyniku:**
    *   **Sukces:**
        *   Jeśli zadanie `signIn...` zakończy się sukcesem, `ViewModel` sprawdza, czy `firebaseAuth.currentUser?.isEmailVerified` jest `true`.
        *   **Obsługa "Zapamiętaj mnie":** Jeśli `rememberMe` jest `true`, `ViewModel` wywołuje odpowiednią funkcję w repozytorium, aby zapisać dane uwierzytelniające w bezpieczny sposób (np. w `Encrypted SharedPreferences`). Jeśli `rememberMe` jest `false`, wszelkie wcześniej zapisane dane są usuwane.
        *   Jeśli e-mail jest zweryfikowany, `ViewModel` emituje jednorazowe zdarzenie `NavigationEvent.NavigateToMain`.
        *   Jeśli e-mail nie jest zweryfikowany, `ViewModel` wystawia stan błędu `State.Error("Konto nie zostało zweryfikowane. Sprawdź e-mail.")`.
    *   **Błąd:**
        *   Jeśli zadanie się nie powiedzie (np. nieprawidłowe dane logowania, użytkownik nie istnieje, błąd sieci), `ViewModel` przechwytuje wyjątek z Firebase, mapuje go na zrozumiały komunikat (np. "Nieprawidłowy e-mail lub hasło.") i wystawia stan `State.Error`.
5.  **Komunikacja z UI:**
    *   Interfejs (`LoginScreen`) obserwuje stan (`State`) wystawiany przez `ViewModel` i reaguje na bieżąco, pokazując komunikaty o błędach, sukcesie lub wskaźnik ładowania.
    *   **Obsługa Nawigacji (Side-effects):**
        *   Do obsługi jednorazowych zdarzeń (jak nawigacja) używany jest `LaunchedEffect`, który nasłuchuje na `SharedFlow` z `ViewModelu`. Gwarantuje to, że nawigacja zostanie wywołana tylko raz.
        *   Po otrzymaniu zdarzenia `NavigateToMain`, wywoływana jest funkcja `navController.navigate("main")` ze specjalną opcją: `popUpTo("login") { inclusive = true }`.
        *   **Zasada Działania `popUpTo`:** Ten mechanizm czyści historię nawigacji (backstack). Usuwa wszystkie ekrany aż do ekranu `login` włącznie. W rezultacie, po pomyślnym zalogowaniu, użytkownik nie może wrócić do ekranu logowania za pomocą systemowego przycisku "wstecz", co jest kluczowe dla poprawnego UX.

### 7.3. Implementacja Etapu 3: Szkielet UI i Nawigacja Główna
Etap 3 został w pełni zrealizowany. Wprowadzono następujące elementy:
*   Główny ekran aplikacji (`MainScreen.kt`) został zbudowany w oparciu o komponent `Scaffold` z Jetpack Compose, zapewniając spójną ramę dla całej aplikacji.
*   Zaimplementowano `BottomNavigationBar`, która umożliwia nawigację między pięcioma głównymi sekcjami: "Główna", "Graj", "Mecze", "Statystyki" i "Ludzie". Definicje tych zakładek (trasa, ikona, tytuł) są zarządzane w osobnym, łatwo edytowalnym pliku `BottomNavItem.kt`.
*   Dla każdej z głównych sekcji utworzono puste ekrany (`HomeScreen`, `PlayScreen`, `MatchHistoryScreen`, `StatsScreen`, `CommunityScreen`, `ProfileScreen`), które są wyświetlane po wybraniu odpowiedniej zakładki w dolnym pasku nawigacyjnym lub wywołaniu z `TopAppBar`.
*   `TopAppBar` został zaimplementowany w `MainScreen.kt`. Zawiera statyczny tytuł "Snooker Stats", ikonę profilu (nawigującą do `ProfileScreen` za pomocą `internalNavController`) oraz ikonę wylogowania (nawigującą do ekranu `login` za pomocą `navController` z `MainActivity`, z opcją `popUpTo("login") { inclusive = true }` do czyszczenia stosu).
*   W formularzu logowania (`LoginScreen.kt`) obsługa klawisza `Tab` w polu e-mail oraz klawisza `Enter` w polu hasła została zaimplementowana za pomocą modyfikatora `onKeyEvent`, zapewniając płynne i oczekiwane działanie.
