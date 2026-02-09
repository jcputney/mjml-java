import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

const config: Config = {
  title: 'mjml-java',
  tagline: 'Pure Java MJML-to-HTML renderer',
  favicon: 'img/favicon.ico',

  url: 'https://jcputney.github.io',
  baseUrl: '/mjml-java/',

  organizationName: 'jcputney',
  projectName: 'mjml-java',

  onBrokenLinks: 'throw',

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  markdown: {
    mermaid: true,
  },

  themes: ['@docusaurus/theme-mermaid'],

  presets: [
    [
      'classic',
      {
        docs: {
          sidebarPath: './sidebars.ts',
          editUrl:
            'https://github.com/jcputney/mjml-java/tree/main/docs/',
        },
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  themeConfig: {
    navbar: {
      title: 'mjml-java',
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'docsSidebar',
          position: 'left',
          label: 'Getting Started',
        },
        {
          to: '/docs/api-reference/',
          label: 'API Reference',
          position: 'left',
        },
        {
          to: '/docs/guides/custom-components',
          label: 'Guides',
          position: 'left',
        },
        {
          to: '/docs/architecture/',
          label: 'Architecture',
          position: 'left',
        },
        {
          href: 'https://github.com/jcputney/mjml-java',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Getting Started',
              to: '/docs/getting-started/installation',
            },
            {
              label: 'API Reference',
              to: '/docs/api-reference/',
            },
            {
              label: 'Guides',
              to: '/docs/guides/custom-components',
            },
          ],
        },
        {
          title: 'Resources',
          items: [
            {
              label: 'MJML Documentation',
              href: 'https://mjml.io/documentation/',
            },
            {
              label: 'Javadoc',
              href: 'https://jcputney.github.io/mjml-java/apidocs/',
            },
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/jcputney/mjml-java',
            },
            {
              label: 'MJML.io',
              href: 'https://mjml.io/',
            },
          ],
        },
      ],
      copyright: `Copyright ${new Date().getFullYear()} mjml-java contributors. Built with Docusaurus.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
      additionalLanguages: ['java', 'xml-doc', 'bash', 'json'],
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
