package com.diamond_diaries.e2e;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.*;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class E2ETest {

    static Playwright playwright;
    static Browser browser;
    private static final String baseUrl = "https://diamond-diaries-next.vercel.app";
    private static final String email = "alizaghloul360@gmail.com";
    private static final String password = "Test123!";

    BrowserContext context;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(false)
                        .setSlowMo(50)
        );
    }

    @AfterAll
    static void closeBrowser() {
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext(
                new Browser.NewContextOptions().setBaseURL(baseUrl)
        );
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        if (context != null) context.close();
    }

    @Test
    void fullAppTest() throws InterruptedException {
        // 1) Login
        page.navigate("/");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        assertEquals(baseUrl + "/login", page.url());
        page.locator("input[name='email']").fill(email);
        page.locator("input[name='password']").fill(password);
        page.locator("button:has-text('LOG IN')").click();
        page.waitForURL("/");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("screenshots/01_logged_in.png")));

        // 2) Create a new entry
        Thread.sleep(2000);
        page.locator("a[href='/entries']").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.locator("button[aria-label='New Entry']").click();
        page.locator("input[name='title']").fill("title");
        page.locator("[name='content']").fill("new entry content");
        assertEquals("Word Count: 3",
                page.locator("p.text-sm.text-gray-500.mt-1").innerText().trim());
        page.locator("button:has-text('Create Entry')").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("screenshots/02_entry_created.png")));

        // 3) Tag + favorite
        Locator firstItem = page.locator("ul.space-y-4 > li").first();
        firstItem.locator("button:has-text('+ Add Tag')").click();
        firstItem.locator("div.flex.flex-wrap.gap-2").waitFor();
        firstItem.locator("div.flex.flex-wrap.gap-2 span").first().click();
        Locator badge = firstItem.locator("span[data-slot='badge']").first();
        assertEquals("Morning Thoughts", badge.innerText().trim());
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("screenshots/03_tag_added.png")));

        Locator emptyHeart = page.locator("div[data-testid='favorite-toggle'] svg").first();
        emptyHeart.click();
        page.waitForSelector("div[data-testid='favorite-toggle'] svg.text-red-500");
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("screenshots/04_favorite_toggled.png")));

        // 4) Create folder
        page.locator("a[href='/folders']").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.locator("button[aria-label='Create new folder']").click();
        page.locator("input[name='name']").fill("new folder");
        page.locator("button:has-text('Create')").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("screenshots/05_folder_created.png")));

        // 5) Delete folder
        Locator firstFolderCard = page.locator("div.grid.grid-cols-1")
                .locator("div.relative").first();
        firstFolderCard.locator("button[aria-label^='Delete folder']").click();
        page.locator("div.absolute.inset-0 button:has-text('Delete')").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("screenshots/06_folder_deleted.png")));

        // 6) Delete entry
        page.locator("a[href='/entries']").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Locator firstEntry_ = page.locator("ul.space-y-4 > li").first();
        firstEntry_.locator("button[data-testid='delete-button']").click();
        page.locator("button[data-testid='confirm-delete-button']").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("screenshots/07_entry_deleted.png")));

        // 7) Account toggles
        page.locator("button[aria-label='Open user menu']").click();
        page.locator("a[href='/account']").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.locator("button[aria-label='Close sidebar']").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);

        // 2FA toggle (no save)
        page.locator("label:has-text('Enable 2FA')").locator("..")
                .locator("button").click();
        page.reload();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("screenshots/08_2fa_not_saved.png")));

        // AI Title Access toggle + save
        Locator toggleAI = page.locator("label:has-text('Allow AI Title Access')").locator("..").locator("button");
        toggleAI.click();
        page.locator("button:has-text('Save Changes')").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("screenshots/09_ai_saved.png")));

        // reset AI toggle for next run
        page.reload();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Locator toggleAIOff = page.locator("label:has-text('Allow AI Title Access')").locator("..").locator("button");
        toggleAIOff.click();
        page.locator("button:has-text('Save Changes')").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);

        // 8) Logout
        page.locator("button[aria-label='Open user menu']").click();
        page.locator("button:has-text('Logout')").click();
        page.waitForURL("/login");
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("screenshots/10_logged_out.png")));
        assertEquals(baseUrl + "/login", page.url());
    }
}
