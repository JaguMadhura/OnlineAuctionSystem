package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import jakarta.servlet.http.HttpSession; // <--- ADD THIS for login session

@Controller
public class AuctionController {

    @Autowired
    private ProductRepository productRepository;

    // --- 1. LOGIN PAGE LOGIC ---

    // Show the login page
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // Looks for login.html
    }

    // Handle the login form submission
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        // Simple Hardcoded Login for demo: username=admin, password=admin123
        if ("admin".equals(username) && "admin123".equals(password)) {
            session.setAttribute("user", username); // "Saves" the user into memory
            return "redirect:/"; // Go to auction page
        } else {
            model.addAttribute("error", "Invalid Username or Password!");
            return "login"; // Stay on login page with error
        }
    }

    // Handle Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Clears the session memory
        return "redirect:/login";
    }

    // --- 2. UPDATED AUCTION LOGIC (With Security Check) ---

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        // SECURITY CHECK: If user is not logged in, send them to login page
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("loggedInUser", session.getAttribute("user"));
        return "index";
    }

    @PostMapping("/add-product")
    public String addProduct(@RequestParam String name, @RequestParam double basePrice, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";

        Product product = new Product();
        product.setName(name);
        product.setBasePrice(basePrice);
        product.setHighestBid(basePrice);
        product.setHighestBidder("No bids yet");
        productRepository.save(product);
        return "redirect:/";
    }

    @PostMapping("/bid/{id}")
    public String placeBid(@PathVariable Long id, @RequestParam double amount, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";

        // Get the logged-in username automatically from the session
        String currentBidder = (String) session.getAttribute("user");

        Product product = productRepository.findById(id).orElseThrow();
        if (amount > product.getHighestBid()) {
            product.setHighestBid(amount);
            product.setHighestBidder(currentBidder); // Automatically use logged-in name
            productRepository.save(product);
        }
        return "redirect:/";
    }
}