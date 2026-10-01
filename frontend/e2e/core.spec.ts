import { test, expect } from '@playwright/test';

test.describe('Fake Company Detector E2E', () => {
  test('should display home page correctly', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('text=Check Before You Apply.')).toBeVisible();
  });

  test('anonymous user can run a full company check', async ({ page }) => {
    await page.goto('/');
    await page.fill('input[placeholder="e.g. Acme Corp"]', 'Playwright Test Company');
    await page.fill('input[placeholder="e.g. acme-corp.com"]', 'playwright-test.com');
    await page.click('button:has-text("Analyze")');
    
    // Wait for the result page to load
    await expect(page.locator('text=Analysis Results')).toBeVisible({ timeout: 15000 });
    await expect(page.locator('text=Playwright Test Company')).toBeVisible();
  });

  test('login flow works', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="email"]', 'admin@example.com');
    await page.fill('input[type="password"]', 'adminpassword');
    await page.click('button[type="submit"]');

    // Wait to be redirected to home or dashboard
    await expect(page.locator('text=Check Before You Apply.')).toBeVisible();
    await expect(page.locator('nav')).toContainText('Logout');
  });

  test('SQL injection attempts are neutralized', async ({ page }) => {
    await page.goto('/');
    await page.fill('input[placeholder="e.g. Acme Corp"]', "Robert'); DROP TABLE users;--");
    await page.fill('input[placeholder="e.g. acme-corp.com"]', 'sql-injection.com');
    await page.click('button:has-text("Analyze")');
    
    // Ensure the system didn't crash and we get a normal result page
    await expect(page.locator('text=Analysis Results')).toBeVisible({ timeout: 15000 });
  });

  test('XSS attempts are neutralized', async ({ page }) => {
    await page.goto('/');
    const xssString = "<script>alert('xss')</script>";
    await page.fill('input[placeholder="e.g. Acme Corp"]', xssString);
    await page.fill('input[placeholder="e.g. acme-corp.com"]', 'xss-test.com');
    await page.click('button:has-text("Analyze")');
    
    // Ensure the result page renders the exact string as text, not HTML
    await expect(page.locator('text=Analysis Results')).toBeVisible({ timeout: 15000 });
    const content = await page.content();
    // It should contain the escaped version or at least not have executed the alert. 
    // Playwright would catch unhandled alerts if we attached a listener, but this checks rendering.
    expect(content).toContain('&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;' || "<script>alert('xss')</script>");
  });
});
