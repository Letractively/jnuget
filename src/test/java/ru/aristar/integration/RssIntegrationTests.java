package ru.aristar.integration;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Интеграционные тесты сайта
 *
 * @author sviridov
 */
public class RssIntegrationTests {

    /**
     * Корневой URL содержит страницу с общей информацией
     *
     * @throws Exception ошибка в процессе теста
     */
    @Test
    public void testDoGet() throws Exception {
        //GIVEN
        WebConversation webConversation = new WebConversation();
        //WHEN
        WebResponse response = webConversation.getResponse("http://localhost:8088");
        //THEN
        assertTrue(response.getText().contains("You are running JNuGet.Server"));
    }

    /**
     * Тест получения заголовка RSS со списком пакетов
     *
     * @throws Exception ошибка в процессе теста
     */
    @Test
    public void testGetPackageList() throws Exception {
        //GIVEN
        WebConversation webConversation = new WebConversation();
        //WHEN
        WebResponse response = webConversation.getResponse("http://localhost:8088/nuget/nuget/Packages");
        //THEN
        assertTrue(response.getText().contains("type=\"text\">Packages<"));
    }
}
