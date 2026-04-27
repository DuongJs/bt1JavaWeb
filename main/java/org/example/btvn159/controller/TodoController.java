package org.example.btvn159.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.btvn159.model.entity.Todo;
import org.example.btvn159.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class TodoController {
    @Autowired
    private TodoRepository todoRepository;

    @GetMapping("/")
    public String welcome(HttpSession session) {
        if (hasOwner(session)) {
            return "redirect:/todos";
        }
        return "welcome";
    }

    @PostMapping("/welcome")
    public String saveOwnerName(@RequestParam("ownerName") String ownerName,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (ownerName == null || ownerName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("messageKey", "owner.required");
            return "redirect:/";
        }

        session.setAttribute("ownerName", ownerName.trim());
        return "redirect:/todos";
    }

    @GetMapping("/list")
    public String redirectOldList() {
        return "redirect:/todos";
    }

    @GetMapping("/todos")
    public String listTodo(Model model, HttpSession session) {
        if (!hasOwner(session)) {
            return "redirect:/";
        }

        model.addAttribute("todos", todoRepository.findAll());
        return "list";
    }

    @GetMapping("/form")
    public String showForm(Model model, HttpSession session) {
        if (!hasOwner(session)) {
            return "redirect:/";
        }

        model.addAttribute("todo", new Todo());
        return "form";
    }

    @GetMapping("/todos/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               Model model,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!hasOwner(session)) {
            return "redirect:/";
        }

        Optional<Todo> todoOptional = todoRepository.findById(id);
        if (todoOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("messageKey", "message.todo.notFoundEdit");
            return "redirect:/todos";
        }
        model.addAttribute("todo", todoOptional.get());
        return "form";
    }

    @PostMapping("/form")
    public String addTodo(@Valid @ModelAttribute("todo") Todo todo,
                          BindingResult bindingResult,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        if (!hasOwner(session)) {
            return "redirect:/";
        }

        if (bindingResult.hasErrors()) {
            return "form";
        }

        boolean isUpdate = todo.getId() != null;
        if (isUpdate && !todoRepository.existsById(todo.getId())) {
            redirectAttributes.addFlashAttribute("messageKey", "message.todo.notFoundUpdate");
            return "redirect:/todos";
        }

        todoRepository.save(todo);
        if (isUpdate) {
            redirectAttributes.addFlashAttribute("messageKey", "message.todo.updated");
        } else {
            redirectAttributes.addFlashAttribute("messageKey", "message.todo.created");
        }

        return "redirect:/todos";
    }

    @GetMapping("/todos/delete/{id}")
    public String deleteTodo(@PathVariable Long id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (!hasOwner(session)) {
            return "redirect:/";
        }

        if (!todoRepository.existsById(id)) {
            redirectAttributes.addFlashAttribute("messageKey", "message.todo.notFoundDelete");
            return "redirect:/todos";
        }

        todoRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("messageKey", "message.todo.deleted");
        return "redirect:/todos";
    }

    private boolean hasOwner(HttpSession session) {
        Object ownerName = session.getAttribute("ownerName");
        return ownerName instanceof String && !((String) ownerName).trim().isEmpty();
    }
}
