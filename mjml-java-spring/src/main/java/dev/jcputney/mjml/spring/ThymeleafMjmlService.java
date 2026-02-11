package dev.jcputney.mjml.spring;

import java.util.Locale;
import java.util.Map;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Service that combines Thymeleaf template processing with MJML rendering. First processes
 * Thymeleaf expressions, then renders the resulting MJML to HTML.
 */
public class ThymeleafMjmlService {

  private final TemplateEngine templateEngine;
  private final MjmlService mjmlService;

  /**
   * Creates a Thymeleaf + MJML composition service.
   *
   * @param templateEngine Thymeleaf engine used for template processing
   * @param mjmlService MJML rendering service applied to processed template output
   */
  public ThymeleafMjmlService(TemplateEngine templateEngine, MjmlService mjmlService) {
    this.templateEngine = templateEngine;
    this.mjmlService = mjmlService;
  }

  /**
   * Processes an inline MJML template string through Thymeleaf, then renders to HTML.
   *
   * @param mjmlTemplate the MJML template string with Thymeleaf expressions
   * @param variables the template variables
   * @return the rendered HTML string
   */
  public String render(String mjmlTemplate, Map<String, Object> variables) {
    Context context = new Context(Locale.getDefault(), variables);
    String processedMjml = templateEngine.process(mjmlTemplate, context);
    return mjmlService.render(processedMjml);
  }

  /**
   * Loads a template by name via Thymeleaf, processes it, then renders as MJML to HTML.
   *
   * @param templateName the Thymeleaf template name
   * @param variables the template variables
   * @return the rendered HTML string
   */
  public String renderTemplate(String templateName, Map<String, Object> variables) {
    Context context = new Context(Locale.getDefault(), variables);
    String processedMjml = templateEngine.process(templateName, context);
    return mjmlService.render(processedMjml);
  }
}
