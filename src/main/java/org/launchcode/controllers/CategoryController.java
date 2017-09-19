package org.launchcode.controllers;

import org.launchcode.models.Category;
import org.launchcode.models.Cheese;
import org.launchcode.models.data.CategoryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryDao categoryDao;

    @RequestMapping("")
    public String index(Model model){

        model.addAttribute("title", "Categories");
        model.addAttribute("categories", categoryDao.findAll());

        return "category/index";
    }
    @RequestMapping(value = "add", method = RequestMethod.GET)
    public String displayAddCategoryForm(Model model){

        model.addAttribute("title", "Add Category");
        model.addAttribute(new Category());

        return "category/add";
    }
    @RequestMapping(value = "add", method = RequestMethod.POST)
    public String processAddCategoryForm(Model model, @ModelAttribute @Valid Category newCategory, Errors errors){

        if (errors.hasErrors()){
            model.addAttribute("title", "Add Category");
            return "category/add";
        }

        categoryDao.save(newCategory);
        return "redirect:/category/view/?id="+newCategory.getId();
    }

    @RequestMapping(value= "view", method = RequestMethod.GET)
    public String viewOneCategory(Model model, @RequestParam int id){
        Category aCategory = categoryDao.findOne(id);
        List<Cheese> cheeses = aCategory.getCheeses();
        model.addAttribute("cheeses", cheeses);
        model.addAttribute("title", "Cheeses in Category: "+aCategory.getName());
        return "cheese/index";
    }
}
