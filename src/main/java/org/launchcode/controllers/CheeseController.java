package org.launchcode.controllers;

import org.launchcode.models.Category;
import org.launchcode.models.Cheese;
import org.launchcode.models.Menu;
import org.launchcode.models.data.CategoryDao;
import org.launchcode.models.data.CheeseDao;
import org.launchcode.models.data.MenuDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by LaunchCode
 */
@Controller
@RequestMapping("cheese")
public class CheeseController {

    @Autowired
    CheeseDao cheeseDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    MenuDao menuDao;

    // Request path: /cheese
    @RequestMapping(value = "")
    public String index(Model model) {

        model.addAttribute("cheeses", cheeseDao.findAll());
        model.addAttribute("title", "The Cheeses");

        return "cheese/index";
    }

    @RequestMapping(value = "add", method = RequestMethod.GET)
    public String displayAddCheeseForm(Model model) {
        model.addAttribute("title", "Add Cheese");
        model.addAttribute(new Cheese());
        model.addAttribute("categories", categoryDao.findAll());
        model.addAttribute("menus", menuDao.findAll());
        return "cheese/add";
    }

    @RequestMapping(value = "add", method = RequestMethod.POST)
    public String processAddCheeseForm(@ModelAttribute  @Valid Cheese newCheese, Errors errors, Model model,
                                       @RequestParam int categoryId,
                                       @RequestParam(value="menuIds", required = false) int[] menuIds) {

        if (errors.hasErrors()) {
            model.addAttribute("title", "Add Cheese");
            model.addAttribute("categories", categoryDao.findAll());
            model.addAttribute("menus", menuDao.findAll());
            return "cheese/add";
        }
        Category aCategory = categoryDao.findOne(categoryId);
        newCheese.setCategory(aCategory);
        cheeseDao.save(newCheese);

        if (menuIds != null) {
            ArrayList<Integer> newMenuIds = new ArrayList<>();
            for (int menuId : menuIds) {
                Integer integerObj = new Integer(menuId);
                newMenuIds.add(integerObj);
            }
            for (Menu menu : menuDao.findAll(newMenuIds)) {
                menu.addItem(newCheese);
                menuDao.save(menu);
            }
        }
        return "redirect:";
    }

    @RequestMapping(value = "remove", method = RequestMethod.GET)
    public String displayRemoveCheeseForm(Model model) {
        model.addAttribute("cheeses", cheeseDao.findAll());
        model.addAttribute("title", "Remove Cheese");
        return "cheese/remove";
    }

    @RequestMapping(value = "remove", method = RequestMethod.POST)
    public String processRemoveCheeseForm(@RequestParam int[] cheeseIds) {

        for (int cheeseId : cheeseIds) {
            List<Menu> relevantMenus = cheeseDao.findOne(cheeseId).getMenus();
            if (relevantMenus.size() > 0) {
                for (Menu menu : relevantMenus) {
                    menu.removeItem(cheeseDao.findOne(cheeseId));
                }
            }
            cheeseDao.delete(cheeseId);
        }

        return "redirect:/cheese";
    }

    @RequestMapping(value= "category", method = RequestMethod.GET)
    public String category(Model model, @RequestParam int id){
        Category aCategory = categoryDao.findOne(id);
        List<Cheese> cheeses = aCategory.getCheeses();
        model.addAttribute("cheeses", cheeses);
        model.addAttribute("title", "Cheeses in Category: "+aCategory.getName());
        return "cheese/index";
    }

    /*@RequestMapping(value= "edit-choose", method= RequestMethod.GET)
    public String chooseACheeseToEdit(Model model){
        model.addAttribute("title", "Choose a cheese to edit:");
        model.addAttribute("cheeses", cheeseDao.findAll());

        return "cheese/edit-choose";
    }*/

    @RequestMapping(value = "edit", method = RequestMethod.GET)
    public String displayEditForm(Model model, @RequestParam int id){
        model.addAttribute("title", "Edit "+cheeseDao.findOne(id).getName());
        model.addAttribute(new Cheese());
        model.addAttribute("categories", categoryDao.findAll());
        model.addAttribute("menus", menuDao.findAll());
        model.addAttribute("givenCheese", cheeseDao.findOne(id));

        return "cheese/edit";
    }

    @RequestMapping(value = "edit", method = RequestMethod.POST)
    public String processEditForm(@ModelAttribute  @Valid Cheese tempCheese, Errors errors, Model model,
                                  @RequestParam int categoryId, @RequestParam int givenCheeseId,
                                  @RequestParam(value="menuIds", required = false) int[] menuIds){
        //check to see if tempCheese has valid fields
        if (errors.hasErrors()){
            model.addAttribute("title", "Edit "+cheeseDao.findOne(givenCheeseId).getName());
            model.addAttribute("categories", categoryDao.findAll());
            model.addAttribute("menus", menuDao.findAll());
            model.addAttribute("givenCheese", cheeseDao.findOne(givenCheeseId));
            return "cheese/edit";
        }
        //update givenCheese to tempCheese's fields
        Cheese givenCheese = cheeseDao.findOne(givenCheeseId);
        givenCheese.setCategory(categoryDao.findOne(categoryId));
        givenCheese.setName(tempCheese.getName());
        givenCheese.setDescription(tempCheese.getDescription());
        cheeseDao.save(givenCheese);
        //update necessary menus to drop/add givenCheese
        if (menuIds != null) {
            //set up desired and old menus into nice ArrayLists
            ArrayList<Integer> desiredMenuIds = new ArrayList<>();
            for (int menuId : menuIds) {
                Integer integerObj = new Integer(menuId);
                desiredMenuIds.add(integerObj);
            }
            ArrayList<Menu> desiredMenus = new ArrayList<>();
            for (Integer desiredMenuId : desiredMenuIds){
                desiredMenus.add(menuDao.findOne(desiredMenuId));
            }
            List<Menu> oldMenus = givenCheese.getMenus();
            //do some loops to compare current vs desired
            for (Menu currentMenu : menuDao.findAll()) {
                if (oldMenus.contains(currentMenu)){
                    if (!desiredMenus.contains(currentMenu)){
                        currentMenu.removeItem(givenCheese);
                        menuDao.save(currentMenu);
                    }
                }else {
                    if (desiredMenus.contains(currentMenu)){
                        currentMenu.addItem(givenCheese);
                        menuDao.save(currentMenu);
                    }
                }
            }
        }else {
            for (Menu menu : givenCheese.getMenus()){
                menu.removeItem(givenCheese);
                menuDao.save(menu);
            }
        }
        return "redirect:/cheese";
    }

}
