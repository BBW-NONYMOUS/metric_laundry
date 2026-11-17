package com.metriclaundry.service;

import com.metriclaundry.dao.UserDAO;
import com.metriclaundry.model.Role;
import com.metriclaundry.model.User;
import com.metriclaundry.util.AppException;
import com.metriclaundry.util.PasswordUtil;
import com.metriclaundry.util.Session;

import java.util.regex.Pattern;

/** Login, registration and account management rules. */
public class AuthService {
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_.]{3,30}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^(09|\\+639)\\d{9}$");

    private final UserDAO users = new UserDAO();

//    Login Logic 
    
    public User login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty())
            throw new AppException("Please enter your username and password.");
        Object[] row = users.findCredentials(username.trim());
        
        // Same message for "unknown user" and "wrong password" so usernames cannot be guessed.
        if (row == null || !PasswordUtil.verify(password, (String) row[2], (String) row[1]))
            throw new AppException("Invalid username or password.");
        User u = (User) row[0];
        if (!u.isActive()) 
            throw new AppException("This account is deactivated. Please contact the shop.");
        
        Session.set(u);
        
        return u;
    }

    /** Self sign-up: always creates a CUSTOMER account. */
    public User registerCustomer(User u, String password, String confirm) {
        u.setRole(Role.CUSTOMER);
        u.setActive(true);
        return createUser(u, password, confirm);
    }

    /** Used by Admin (any role) and Staff (walk-in customers). */
    public User createUser(User u, String password, String confirm) {
        validateProfile(u);
        validatePassword(password, confirm);
        String salt = PasswordUtil.newSalt();
        u.setId(users.insert(u, PasswordUtil.hash(password, salt), salt));
        return u;
    }

    public void updateUser(User u) {
        validateProfile(u);
        User current = Session.get();
        if (current != null && current.getId() == u.getId() && (!u.isActive() || u.getRole() != Role.ADMIN)
                && current.getRole() == Role.ADMIN)
            throw new AppException("You cannot deactivate or demote your own admin account.");
        users.update(u);
    }

    public void changePassword(int userId, String oldPassword, String newPassword, String confirm) {
        User u = users.findById(userId);
        Object[] row = users.findCredentials(u.getUsername());
        if (!PasswordUtil.verify(oldPassword, (String) row[2], (String) row[1]))
            throw new AppException("Current password is incorrect.");
        validatePassword(newPassword, confirm);
        String salt = PasswordUtil.newSalt();
        users.updatePassword(userId, PasswordUtil.hash(newPassword, salt), salt);
    }

    /** Admin reset - no old password needed. */
    public void resetPassword(int userId, String newPassword) {
        validatePassword(newPassword, newPassword);
        String salt = PasswordUtil.newSalt();
        users.updatePassword(userId, PasswordUtil.hash(newPassword, salt), salt);
    }

    private void validateProfile(User u) {
        if (u.getUsername() == null || !USERNAME.matcher(u.getUsername().trim()).matches())
            throw new AppException("Username must be 3-30 characters (letters, numbers, . or _).");
        u.setUsername(u.getUsername().trim());
        if (u.getFullName() == null || u.getFullName().isBlank())
            throw new AppException("Full name is required.");
        if (u.getEmail() != null && !u.getEmail().isBlank() && !EMAIL.matcher(u.getEmail().trim()).matches())
            throw new AppException("Please enter a valid email address.");
        if (u.getPhone() != null && !u.getPhone().isBlank()
                && !PHONE.matcher(u.getPhone().replaceAll("[\\s-]", "")).matches())
            throw new AppException("Phone must be a PH mobile number, e.g. 09171234567.");
        if (users.usernameExists(u.getUsername(), u.getId()))
            throw new AppException("Username \"" + u.getUsername() + "\" is already taken.");
    }

    private void validatePassword(String password, String confirm) {
        if (password == null || password.length() < 6)
            throw new AppException("Password must be at least 6 characters.");
        if (!password.equals(confirm))
            throw new AppException("Passwords do not match.");
    }
}
