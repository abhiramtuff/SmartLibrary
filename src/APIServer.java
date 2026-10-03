import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import model.Book;
import model.BookStatus;
import model.BorrowRecord;
import model.Library;
import model.User;

public class APIServer {

    private static Library library;
    private static List<String> recentActivity = new ArrayList<>();
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try {
            // Load library state (DB hydration occurs automatically)
            library = new Library();
            System.out.println("Library State Loaded.");

            // Add some initial logs to recent activity
            recentActivity.add("Library management system initialized.");
            recentActivity.add("Database sync completed: Loaded " + library.getBooks().size() + " books, " + library.getUsers().size() + " users.");

            // Create HTTP Server
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            
            // Set contexts
            server.createContext("/api", new ApiHandler());
            server.createContext("/", new StaticFileHandler());
            
            server.setExecutor(null); // default executor
            server.start();
            
            System.out.println("====================================================");
            System.out.println("  SMART LIBRARY SYSTEM WEB SERVER STARTED           ");
            System.out.println("  Access the application at: http://localhost:" + PORT + "/ ");
            System.out.println("====================================================");
        } catch (Exception e) {
            System.out.println("Failed to start API Server.");
            e.printStackTrace();
        }
    }

    // Add activity log helper
    private static void logActivity(String activity) {
        recentActivity.add(0, "[" + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + activity);
        if (recentActivity.size() > 10) {
            recentActivity.remove(recentActivity.size() - 1);
        }
    }

    // ==========================================
    // STATIC FILES HANDLER
    // ==========================================
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            // Default path mapping for Single Page App router or root index
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            File file = new File("frontend" + path);
            if (!file.exists() || file.isDirectory()) {
                // Return 404
                String response = "404 - File Not Found: " + path;
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            // Set Content-Type based on extension
            String contentType = "text/plain";
            String name = file.getName().toLowerCase();
            if (name.endsWith(".html")) contentType = "text/html";
            else if (name.endsWith(".css")) contentType = "text/css";
            else if (name.endsWith(".js")) contentType = "application/javascript";
            else if (name.endsWith(".json")) contentType = "application/json";
            else if (name.endsWith(".png")) contentType = "image/png";
            else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (name.endsWith(".svg")) contentType = "image/svg+xml";
            else if (name.endsWith(".ico")) contentType = "image/x-icon";

            exchange.getResponseHeaders().set("Content-Type", contentType);
            
            // Enable CORS for static file server just in case
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

            byte[] fileBytes = Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, fileBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(fileBytes);
            os.close();
        }
    }

    // ==========================================
    // API OPERATIONS HANDLER
    // ==========================================
    static class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Configure CORS
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            // Handle preflight pre-checks
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            String response = "";
            int statusCode = 200;

            try {
                // Parse params (GET parameters from query, POST parameters from request body)
                Map<String, String> params = new HashMap<>();
                if ("GET".equalsIgnoreCase(method)) {
                    params = parseQueryParams(exchange.getRequestURI().getQuery());
                } else if ("POST".equalsIgnoreCase(method)) {
                    InputStream is = exchange.getRequestBody();
                    String body = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                            .lines().collect(Collectors.joining("\n"));
                    
                    String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
                    if (contentType != null && contentType.toLowerCase().contains("json")) {
                        params = parseJsonBody(body);
                    } else {
                        params = parseQueryParams(body);
                    }
                }

                // API Route Dispatcher
                if ("/api/dashboard".equals(path) && "GET".equalsIgnoreCase(method)) {
                    response = handleDashboard();
                } 
                else if ("/api/books".equals(path)) {
                    if ("GET".equalsIgnoreCase(method)) {
                        response = handleGetBooks();
                    } else if ("POST".equalsIgnoreCase(method)) {
                        response = handleAddBook(params);
                    }
                } 
                else if ("/api/books/update".equals(path) && "POST".equalsIgnoreCase(method)) {
                    response = handleUpdateBook(params);
                } 
                else if ("/api/books/delete".equals(path) && "POST".equalsIgnoreCase(method)) {
                    response = handleDeleteBook(params);
                } 
                else if ("/api/users".equals(path)) {
                    if ("GET".equalsIgnoreCase(method)) {
                        response = handleGetUsers();
                    } else if ("POST".equalsIgnoreCase(method)) {
                        response = handleAddUser(params);
                    }
                } 
                else if ("/api/users/update".equals(path) && "POST".equalsIgnoreCase(method)) {
                    response = handleUpdateUser(params);
                } 
                else if ("/api/users/delete".equals(path) && "POST".equalsIgnoreCase(method)) {
                    response = handleDeleteUser(params);
                } 
                else if ("/api/borrow".equals(path) && "POST".equalsIgnoreCase(method)) {
                    response = handleBorrow(params);
                } 
                else if ("/api/return".equals(path) && "POST".equalsIgnoreCase(method)) {
                    response = handleReturn(params);
                } 
                else if ("/api/renew".equals(path) && "POST".equalsIgnoreCase(method)) {
                    response = handleRenew(params);
                } 
                else if ("/api/hold".equals(path) && "POST".equalsIgnoreCase(method)) {
                    response = handleHold(params);
                } 
                else if ("/api/records".equals(path) && "GET".equalsIgnoreCase(method)) {
                    response = handleGetRecords();
                } 
                else {
                    statusCode = 404;
                    response = "{\"success\":false,\"message\":\"Endpoint not found\"}";
                }
            } catch (Exception e) {
                e.printStackTrace();
                statusCode = 500;
                response = "{\"success\":false,\"message\":\"Internal server error: " + escapeJSON(e.getMessage()) + "\"}";
            }

            // Write response
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }

    // ==========================================
    // API ENDPOINT IMPLEMENTATIONS
    // ==========================================

    private static String handleDashboard() {
        int totalBooks = library.getBooks().size();
        int totalUsers = library.getUsers().size();
        int activeRecords = library.getBorrowRecords().size();
        
        long availableBooks = library.getBooks().stream()
                .filter(b -> b.getStatus() == BookStatus.AVAILABLE)
                .count();
                
        long borrowedBooks = library.getBooks().stream()
                .filter(b -> b.getStatus() == BookStatus.BORROWED)
                .count();

        // Build recent activities JSON array
        StringBuilder actJson = new StringBuilder();
        actJson.append("[");
        for (int i = 0; i < recentActivity.size(); i++) {
            actJson.append("\"").append(escapeJSON(recentActivity.get(i))).append("\"");
            if (i < recentActivity.size() - 1) {
                actJson.append(",");
            }
        }
        actJson.append("]");

        return "{" +
               "\"success\":true," +
               "\"totalBooks\":" + totalBooks + "," +
               "\"availableBooks\":" + availableBooks + "," +
               "\"borrowedBooks\":" + borrowedBooks + "," +
               "\"totalUsers\":" + totalUsers + "," +
               "\"activeBorrows\":" + activeRecords + "," +
               "\"recentActivity\":" + actJson.toString() +
               "}";
    }

    private static String handleGetBooks() {
        return "{\"success\":true,\"books\":" + booksToJson(library.getBooks()) + "}";
    }

    private static String handleAddBook(Map<String, String> params) {
        String isbn = params.get("isbn");
        String title = params.get("title");
        String author = params.get("author");
        String genre = params.get("genre");

        if (isEmpty(isbn) || isEmpty(title) || isEmpty(author) || isEmpty(genre)) {
            return "{\"success\":false,\"message\":\"All fields (ISBN, Title, Author, Genre) are required.\"}";
        }

        if (library.searchByIsbn(isbn) != null) {
            return "{\"success\":false,\"message\":\"A book with ISBN " + isbn + " already exists.\"}";
        }

        Book book = new Book(title, author, isbn, genre);
        library.addBook(book);
        logActivity("Added new book: '" + title + "' by " + author + " (ISBN: " + isbn + ")");
        return "{\"success\":true,\"message\":\"Book added successfully!\",\"book\":" + bookToJson(book) + "}";
    }

    private static String handleUpdateBook(Map<String, String> params) {
        String isbn = params.get("isbn");
        String title = params.get("title");
        String author = params.get("author");
        String genre = params.get("genre");

        if (isEmpty(isbn) || isEmpty(title) || isEmpty(author) || isEmpty(genre)) {
            return "{\"success\":false,\"message\":\"All fields (ISBN, Title, Author, Genre) are required.\"}";
        }

        boolean updated = library.updateBook(isbn, title, author, genre);
        if (updated) {
            logActivity("Updated book details for ISBN " + isbn + ": '" + title + "'");
            return "{\"success\":true,\"message\":\"Book details updated successfully!\"}";
        } else {
            return "{\"success\":false,\"message\":\"Book with ISBN " + isbn + " not found.\"}";
        }
    }

    private static String handleDeleteBook(Map<String, String> params) {
        String isbn = params.get("isbn");
        if (isEmpty(isbn)) {
            return "{\"success\":false,\"message\":\"ISBN is required.\"}";
        }

        Book book = library.searchByIsbn(isbn);
        if (book == null) {
            return "{\"success\":false,\"message\":\"Book not found.\"}";
        }

        if (book.getStatus() == BookStatus.BORROWED) {
            return "{\"success\":false,\"message\":\"Cannot delete book because it is currently borrowed.\"}";
        }

        library.removeBook(book);
        logActivity("Deleted book: '" + book.getTitle() + "' (ISBN: " + isbn + ")");
        return "{\"success\":true,\"message\":\"Book removed successfully!\"}";
    }

    private static String handleGetUsers() {
        return "{\"success\":true,\"users\":" + usersToJson(library.getUsers()) + "}";
    }

    private static String handleAddUser(Map<String, String> params) {
        String idStr = params.get("userId");
        String name = params.get("name");
        String contact = params.get("contact");

        if (isEmpty(idStr) || isEmpty(name) || isEmpty(contact)) {
            return "{\"success\":false,\"message\":\"All fields (ID, Name, Contact) are required.\"}";
        }

        int userId;
        try {
            userId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            return "{\"success\":false,\"message\":\"User ID must be a valid integer.\"}";
        }

        if (library.searchUserById(userId) != null) {
            return "{\"success\":false,\"message\":\"A user with ID " + userId + " already exists.\"}";
        }

        User user = new User(userId, name, contact);
        library.registerUser(user);
        logActivity("Registered new user: " + name + " (ID: " + userId + ")");
        return "{\"success\":true,\"message\":\"User registered successfully!\",\"user\":" + userToJson(user) + "}";
    }

    private static String handleUpdateUser(Map<String, String> params) {
        String idStr = params.get("userId");
        String name = params.get("name");
        String contact = params.get("contact");

        if (isEmpty(idStr) || isEmpty(name) || isEmpty(contact)) {
            return "{\"success\":false,\"message\":\"All fields (ID, Name, Contact) are required.\"}";
        }

        int userId;
        try {
            userId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            return "{\"success\":false,\"message\":\"User ID must be a valid integer.\"}";
        }

        boolean updated = library.updateUser(userId, name, contact);
        if (updated) {
            logActivity("Updated user details for ID " + userId + ": " + name);
            return "{\"success\":true,\"message\":\"User details updated successfully!\"}";
        } else {
            return "{\"success\":false,\"message\":\"User with ID " + userId + " not found.\"}";
        }
    }

    private static String handleDeleteUser(Map<String, String> params) {
        String idStr = params.get("userId");
        if (isEmpty(idStr)) {
            return "{\"success\":false,\"message\":\"User ID is required.\"}";
        }

        int userId;
        try {
            userId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            return "{\"success\":false,\"message\":\"User ID must be a valid integer.\"}";
        }

        User user = library.searchUserById(userId);
        if (user == null) {
            return "{\"success\":false,\"message\":\"User not found.\"}";
        }

        if (user.getBorrowedBooks().size() > 0) {
            return "{\"success\":false,\"message\":\"Cannot delete user because they have " + user.getBorrowedBooks().size() + " active borrowed books.\"}";
        }

        library.removeUser(user);
        logActivity("Deleted user: " + user.getName() + " (ID: " + userId + ")");
        return "{\"success\":true,\"message\":\"User removed successfully!\"}";
    }

    private static String handleBorrow(Map<String, String> params) {
        String idStr = params.get("userId");
        String isbn = params.get("isbn");

        if (isEmpty(idStr) || isEmpty(isbn)) {
            return "{\"success\":false,\"message\":\"User ID and ISBN are required.\"}";
        }

        int userId;
        try {
            userId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            return "{\"success\":false,\"message\":\"User ID must be a valid integer.\"}";
        }

        // Perform validations mirroring Library.java logic to capture descriptive errors
        User user = library.searchUserById(userId);
        if (user == null) {
            return "{\"success\":false,\"message\":\"User not found.\"}";
        }

        Book book = library.searchByIsbn(isbn);
        if (book == null) {
            return "{\"success\":false,\"message\":\"Book not found.\"}";
        }

        if (book.getStatus() != BookStatus.AVAILABLE) {
            return "{\"success\":false,\"message\":\"Book is already borrowed.\"}";
        }

        if (user.getBorrowedBooks().size() >= 3) {
            return "{\"success\":false,\"message\":\"Borrowing limit reached. Maximum 3 books allowed.\"}";
        }

        boolean success = library.borrowBook(userId, isbn);
        if (success) {
            logActivity("User " + user.getName() + " (ID: " + userId + ") borrowed '" + book.getTitle() + "'");
            return "{\"success\":true,\"message\":\"Book borrowed successfully!\"}";
        } else {
            return "{\"success\":false,\"message\":\"Borrowing failed. Please check backend constraints.\"}";
        }
    }

    private static String handleReturn(Map<String, String> params) {
        String idStr = params.get("userId");
        String isbn = params.get("isbn");

        if (isEmpty(idStr) || isEmpty(isbn)) {
            return "{\"success\":false,\"message\":\"User ID and ISBN are required.\"}";
        }

        int userId;
        try {
            userId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            return "{\"success\":false,\"message\":\"User ID must be a valid integer.\"}";
        }

        User user = library.searchUserById(userId);
        if (user == null) {
            return "{\"success\":false,\"message\":\"User not found.\"}";
        }

        Book book = library.searchByIsbn(isbn);
        if (book == null) {
            return "{\"success\":false,\"message\":\"Book not found.\"}";
        }

        if (!user.getBorrowedBooks().contains(book)) {
            return "{\"success\":false,\"message\":\"This user has not borrowed this book.\"}";
        }

        // Retrieve borrow record to calculate late fee for the response details
        BorrowRecord record = null;
        for (BorrowRecord r : library.getBorrowRecords()) {
            if (r.getUser().getUserId() == userId && r.getBook().getIsbn().equals(isbn)) {
                record = r;
                break;
            }
        }

        double lateFee = 0.0;
        if (record != null) {
            lateFee = record.calculateLateFee();
        }

        boolean success = library.returnBook(userId, isbn);
        if (success) {
            String logMsg = "User " + user.getName() + " returned '" + book.getTitle() + "'";
            if (lateFee > 0) {
                logMsg += " (Late fee paid: Rs. " + lateFee + ")";
            }
            logActivity(logMsg);
            
            return "{\"success\":true,\"message\":\"Book returned successfully!\",\"lateFee\":" + lateFee + "}";
        } else {
            return "{\"success\":false,\"message\":\"Return failed. Please check backend constraints.\"}";
        }
    }

    private static String handleRenew(Map<String, String> params) {
        String idStr = params.get("userId");
        String isbn = params.get("isbn");

        if (isEmpty(idStr) || isEmpty(isbn)) {
            return "{\"success\":false,\"message\":\"User ID and ISBN are required.\"}";
        }

        int userId;
        try {
            userId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            return "{\"success\":false,\"message\":\"User ID must be a valid integer.\"}";
        }

        User user = library.searchUserById(userId);
        if (user == null) {
            return "{\"success\":false,\"message\":\"User not found.\"}";
        }

        Book book = library.searchByIsbn(isbn);
        if (book == null) {
            return "{\"success\":false,\"message\":\"Book not found.\"}";
        }

        if (!user.getBorrowedBooks().contains(book)) {
            return "{\"success\":false,\"message\":\"This user has not borrowed this book.\"}";
        }

        BorrowRecord record = null;
        for (BorrowRecord r : library.getBorrowRecords()) {
            if (r.getUser().getUserId() == userId && r.getBook().getIsbn().equals(isbn)) {
                record = r;
                break;
            }
        }

        if (record == null) {
            return "{\"success\":false,\"message\":\"Borrowing record not found.\"}";
        }

        if (record.isRenewed()) {
            return "{\"success\":false,\"message\":\"Renewal rejected: Book has already been renewed once.\"}";
        }

        if (record.isOnHold()) {
            return "{\"success\":false,\"message\":\"Renewal rejected: Book cannot be renewed because another user has placed a hold.\"}";
        }

        boolean success = library.renewBook(userId, isbn);
        if (success) {
            logActivity("User " + user.getName() + " renewed '" + book.getTitle() + "' (Extended due date: " + record.getDueDate() + ")");
            return "{\"success\":true,\"message\":\"Book renewed successfully!\",\"newDueDate\":\"" + record.getDueDate() + "\"}";
        } else {
            return "{\"success\":false,\"message\":\"Renewal failed.\"}";
        }
    }

    private static String handleHold(Map<String, String> params) {
        String idStr = params.get("userId");
        String isbn = params.get("isbn");

        if (isEmpty(idStr) || isEmpty(isbn)) {
            return "{\"success\":false,\"message\":\"User ID and ISBN are required.\"}";
        }

        int userId;
        try {
            userId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            return "{\"success\":false,\"message\":\"User ID must be a valid integer.\"}";
        }

        User user = library.searchUserById(userId);
        if (user == null) {
            return "{\"success\":false,\"message\":\"User not found.\"}";
        }

        Book book = library.searchByIsbn(isbn);
        if (book == null) {
            return "{\"success\":false,\"message\":\"Book not found.\"}";
        }

        BorrowRecord record = null;
        for (BorrowRecord r : library.getBorrowRecords()) {
            if (r.getBook().getIsbn().equals(isbn)) {
                record = r;
                break;
            }
        }

        if (record == null) {
            return "{\"success\":false,\"message\":\"Book is not currently borrowed. Holds can only be placed on borrowed books.\"}";
        }

        if (record.getUser().getUserId() == userId) {
            return "{\"success\":false,\"message\":\"The current borrower cannot place a hold.\"}";
        }

        if (record.isOnHold()) {
            return "{\"success\":false,\"message\":\"A hold has already been placed on this book.\"}";
        }

        boolean success = library.placeHold(userId, isbn);
        if (success) {
            logActivity("User " + user.getName() + " placed a hold on '" + book.getTitle() + "'");
            return "{\"success\":true,\"message\":\"Hold placed successfully!\"}";
        } else {
            return "{\"success\":false,\"message\":\"Failed to place hold.\"}";
        }
    }

    private static String handleGetRecords() {
        return "{\"success\":true,\"records\":" + recordsToJson(library.getBorrowRecords()) + "}";
    }

    // ==========================================
    // JSON & PARAMETER UTILITIES
    // ==========================================

    private static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private static String escapeJSON(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    private static String bookToJson(Book book) {
        return "{" +
               "\"isbn\":\"" + escapeJSON(book.getIsbn()) + "\"," +
               "\"title\":\"" + escapeJSON(book.getTitle()) + "\"," +
               "\"author\":\"" + escapeJSON(book.getAuthor()) + "\"," +
               "\"genre\":\"" + escapeJSON(book.getGenre()) + "\"," +
               "\"status\":\"" + book.getStatus().toString() + "\"" +
               "}";
    }

    private static String booksToJson(List<Book> books) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < books.size(); i++) {
            sb.append(bookToJson(books.get(i)));
            if (i < books.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static String userToJson(User user) {
        return "{" +
               "\"userId\":" + user.getUserId() + "," +
               "\"name\":\"" + escapeJSON(user.getName()) + "\"," +
               "\"contact\":\"" + escapeJSON(user.getContact()) + "\"," +
               "\"borrowedCount\":" + user.getBorrowedBooks().size() +
               "}";
    }

    private static String usersToJson(List<User> users) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < users.size(); i++) {
            sb.append(userToJson(users.get(i)));
            if (i < users.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static String recordToJson(BorrowRecord record) {
        return "{" +
               "\"userId\":" + record.getUser().getUserId() + "," +
               "\"userName\":\"" + escapeJSON(record.getUser().getName()) + "\"," +
               "\"isbn\":\"" + escapeJSON(record.getBook().getIsbn()) + "\"," +
               "\"bookTitle\":\"" + escapeJSON(record.getBook().getTitle()) + "\"," +
               "\"borrowDate\":\"" + record.getBorrowDate().toString() + "\"," +
               "\"dueDate\":\"" + record.getDueDate().toString() + "\"," +
               "\"renewed\":" + record.isRenewed() + "," +
               "\"onHold\":" + record.isOnHold() + "," +
               "\"lateFee\":" + record.calculateLateFee() +
               "}";
    }

    private static String recordsToJson(List<BorrowRecord> records) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < records.size(); i++) {
            sb.append(recordToJson(records.get(i)));
            if (i < records.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null || query.trim().isEmpty()) {
            return result;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                String value = (idx > 0 && pair.length() > idx + 1) ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : "";
                result.put(key, value);
            } catch (Exception e) {
                // Ignore
            }
        }
        return result;
    }

    private static Map<String, String> parseJsonBody(String body) {
        Map<String, String> result = new HashMap<>();
        if (body == null || body.trim().isEmpty()) {
            return result;
        }
        body = body.trim();
        if (body.startsWith("{") && body.endsWith("}")) {
            body = body.substring(1, body.length() - 1);
        }
        Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:\"([^\"]*)\"|([^,{}]+))");
        Matcher matcher = pattern.matcher(body);
        while (matcher.find()) {
            String key = matcher.group(1);
            String valStr = matcher.group(2);
            String valNum = matcher.group(3);
            String value = (valStr != null) ? valStr : ((valNum != null) ? valNum.trim() : "");
            // Clean outer quotes from values if parsed as non-quoted fallback unexpectedly
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            result.put(key, value);
        }
        return result;
    }
}
