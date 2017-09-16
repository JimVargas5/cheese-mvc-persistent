package org.launchcode.controllers;

import org.launchcode.models.Cheese;
import org.launchcode.models.Menu;
import org.launchcode.models.data.CheeseDao;
import org.launchcode.models.data.MenuDao;
import org.launchcode.models.forms.AddMenuItemForm;
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
@RequestMapping(value= "menu")
public class MenuController {
    @Autowired
    MenuDao menuDao;

    @Autowired
    CheeseDao cheeseDao;

    @RequestMapping(value = "list")
    public String index(Model model) {
        model.addAttribute("menus", menuDao.findAll());
        model.addAttribute("title", "The Menus");

        return "menu/index";
    }

    @RequestMapping(value = "add", method = RequestMethod.GET)
    public String displayAddMenuForm(Model model){
        model.addAttribute("title", "Add a Menu");
        model.addAttribute(new Menu());

        return "menu/add";
    }

    @RequestMapping(value = "add", method = RequestMethod.POST)
    public String processAddMenuForm(@ModelAttribute @Valid Menu newMenu, Errors errors,
                                     Model model){
        if (errors.hasErrors()){
            model.addAttribute("title", "Add a Menu");

            return "menu/add";
        }
        menuDao.save(newMenu);
        return "redirect:/menu/?id="+newMenu.getId();
    }

    @RequestMapping(value= "", method = RequestMethod.GET)
    public String viewMenu(Model model, @RequestParam int id){
        Menu aMenu = menuDao.findOne(id);
        //List<Cheese> cheeses = aMenu.getCheeses();
        model.addAttribute("menu", aMenu);
        model.addAttribute("title", "Cheeses in Menu: "+aMenu.getName());
        return "menu/view";
    }

    @RequestMapping(value = "add-item", method = RequestMethod.GET)
    public String displayAddItemForm(Model model, @RequestParam int id){
        Menu givenMenu = menuDao.findOne(id);
        AddMenuItemForm form = new AddMenuItemForm(givenMenu, cheeseDao.findAll());

        model.addAttribute("form", form);
        model.addAttribute("menu", givenMenu);
        model.addAttribute("title", "Add item to menu:"+givenMenu.getName());
        return "menu/add-item";
    }

    @RequestMapping(value = "add-item", method = RequestMethod.POST)
    public String processAddItemForm(@ModelAttribute @Valid AddMenuItemForm newAddMenuItemForm,
                                     Errors errors, Model model, @RequestParam int id,
                                     @RequestParam int cheeseId, @RequestParam int menuId){
        if (errors.hasErrors()){
            model.addAttribute("title", "Add item to menu:"+menuDao.findOne(id).getName());

            return "menu/add-item";
        }
        Cheese givenCheese = cheeseDao.findOne(cheeseId);
        Menu givenMenu = menuDao.findOne(menuId);
        givenMenu.addItem(givenCheese);
        menuDao.save(givenMenu);

        return "redirect:/menu/?id="+givenMenu.getId();
    }

}
