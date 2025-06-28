package com.mdm.reviewdashboard.controller;

import com.mdm.reviewdashboard.domain.MDMEntity;
import com.mdm.reviewdashboard.domain.MergeCandidatePair;
import com.mdm.reviewdashboard.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for the human review dashboard.
 * Handles displaying pending merge candidates and processing approval/rejection actions.
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final ReviewService reviewService;

    @Autowired
    public DashboardController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Displays the main dashboard page with a list of pending merge candidates.
     * @param model The Spring UI Model.
     * @return The name of the Thymeleaf template to render.
     */
    @GetMapping
    public String viewDashboard(Model model) {
        List<MergeCandidatePair> pendingCandidates = reviewService.getPendingMergeCandidates();
        model.addAttribute("candidates", pendingCandidates);
        model.addAttribute("reviewService", reviewService); // Pass service to template for deserialization
        return "dashboard"; // Refers to src/main/resources/templates/dashboard.html
    }

    /**
     * Handles the approval or rejection of a merge candidate.
     * @param id The ID of the merge candidate pair.
     * @param action The action to perform ("approve" or "reject").
     * @param comment The reviewer's comment.
     * @param redirectAttributes For adding flash attributes to the redirect.
     * @return Redirects back to the dashboard.
     */
    @PostMapping("/update-status")
    public String updateCandidateStatus(
            @RequestParam Long id,
            @RequestParam String action,
            @RequestParam(required = false) String comment,
            RedirectAttributes redirectAttributes) {

        Optional<MergeCandidatePair> updatedPair = Optional.empty();
        String message;

        if ("approve".equalsIgnoreCase(action)) {
            updatedPair = reviewService.approveMergeCandidate(id, comment);
            message = "Merge candidate " + id + " approved successfully.";
        } else if ("reject".equalsIgnoreCase(action)) {
            updatedPair = reviewService.rejectMergeCandidate(id, comment);
            message = "Merge candidate " + id + " rejected successfully.";
        } else {
            message = "Invalid action: " + action;
            redirectAttributes.addFlashAttribute("errorMessage", message);
            return "redirect:/dashboard";
        }

        if (updatedPair.isPresent()) {
            redirectAttributes.addFlashAttribute("successMessage", message);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update merge candidate " + id + ".");
        }

        return "redirect:/dashboard";
    }
}