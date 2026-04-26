package org.example.btvn159.controller;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class TodoController {
    @Autowired
    private TodoRepository todoRepository;

    @GetMapping("/")
    public String redirectToList() {
        return "redirect:/list";
    }

    @GetMapping("/list")
    public String listTodo(Model model) {
        model.addAttribute("todos", todoRepository.findAll());
        return "list";
    }

    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("todo", new Todo());
        return "form";
    }

    @GetMapping("/todos/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Optional<Todo> todoOptional = todoRepository.findById(id);
        if (todoOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy công việc cần sửa!");
            return "redirect:/list";
        }
        model.addAttribute("todo", todoOptional.get());
        return "form";
    }

    @PostMapping("/form")
    public String addTodo(@Valid @ModelAttribute("todo") Todo todo,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "form";
        }

        boolean isUpdate = todo.getId() != null;
        if (isUpdate && !todoRepository.existsById(todo.getId())) {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy công việc cần cập nhật!");
            return "redirect:/list";
        }

        todoRepository.save(todo);
        if (isUpdate) {
            redirectAttributes.addFlashAttribute("message", "Cập nhật công việc thành công!");
        } else {
            redirectAttributes.addFlashAttribute("message", "Thêm công việc thành công!");
        }

        return "redirect:/list";
    }

    @GetMapping("/todos/delete/{id}")
    public String deleteTodo(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        if (!todoRepository.existsById(id)) {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy công việc để xóa!");
            return "redirect:/list";
        }

        todoRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Xóa công việc thành công!");
        return "redirect:/list";
    }
}
