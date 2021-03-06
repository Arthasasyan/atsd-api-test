package com.axibase.tsd.api.method.property.command;

import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.method.property.PropertyMethod;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.command.PropertyCommand;
import com.axibase.tsd.api.model.extended.CommandSendingResult;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.util.Mocks;
import com.google.common.collect.ImmutableMap;
import io.qameta.allure.Description;
import io.qameta.allure.Issue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.method.property.PropertyTest.assertPropertyExisting;
import static org.testng.AssertJUnit.assertEquals;

public class EmptyTagTest extends PropertyMethod {

    @DataProvider
    private Object[][] emptyValues() {
        return new String[][] {
                {""},
                {"\r"},
                {"\n"},
                {"\r\n"},
                {" "},
                {"  "},
                {" \n "},
                {" \n"},
                {"\n "}
        };
    }

    @Issue("6234")
    @Description("Tests that if the only tag in insertion command is empty, it fails.")
    @Test(
            dataProvider = "emptyValues"
    )
    public void emptyTagFailTest(String emptyValue) {
        Property property = new Property(Mocks.propertyType(), Mocks.entity())
                .setTags(ImmutableMap.of("t1", emptyValue));
        PlainCommand command = new PropertyCommand(property);
        CommandSendingResult result = CommandMethod.send(command);
        assertEquals("The only command had to fail.", new CommandSendingResult(1, 0), result);
    }

    @Issue("6234")
    @Description("Tests that if one tag is empty and one is not, property is inserted and tag with empty value is deleted.")
    @Test(
            dataProvider = "emptyValues"
    )
    public void emptyAndNonEmptyTagTest(String emptyValue) {
        Property property = new Property(Mocks.propertyType(), Mocks.entity())
                .setTags(ImmutableMap.of("t1", "v1", "t2", "v2"));
        PlainCommand commandWithNonEmptyTags = new PropertyCommand(property);
        CommandSendingResult result = CommandMethod.send(commandWithNonEmptyTags);
        assertEquals("The only command had to succeed.", new CommandSendingResult(0, 1), result);

        property.setTags(ImmutableMap.of("t1", "v1-new", "t2", emptyValue));
        PlainCommand emptyTagCommand = new PropertyCommand(property);
        CommandSendingResult newResult = CommandMethod.send(emptyTagCommand);
        assertEquals("The only command had to succeed.", new CommandSendingResult(0, 1), newResult);

        property.setTags(ImmutableMap.of("t1", "v1-new"));
        assertPropertyExisting(property);
    }

    @Issue("6234")
    @Description("Tests that if key is not empty and the only tg is empty, property is not inserted.")
    @Test(
            dataProvider = "emptyValues"
    )
    public void keyAndEmptyTagTest(String emptyValue) {
        Property property = new Property(Mocks.propertyType(), Mocks.entity())
                .setKey(ImmutableMap.of("k1", "vk1"))
                .setTags(ImmutableMap.of("t1", emptyValue));
        PlainCommand command = new PropertyCommand(property);
        CommandSendingResult result = CommandMethod.send(command);
        assertEquals("The only command had to fail.", new CommandSendingResult(1, 0), result);
    }
}
