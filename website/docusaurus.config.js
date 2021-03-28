/** @type {import('@docusaurus/types').DocusaurusConfig} */
module.exports = {
  title: 'osm4scala',
  tagline: 'Scala library and Spark Polyglot connector for OpenStreetMap Pbf files',
  url: 'https://simplexspatial.github.io/osm4scala/',
  baseUrl: '/osm4scala/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'favicon.ico',
  organizationName: 'simplexspatial',
  projectName: 'osm4scala',
  themeConfig: {
    navbar: {
      title: 'osm4scala',
      logo: {
        alt: 'osm4scala logo',
        src: 'img/logo.svg',
      },
      items: [
        {
          to: 'docs/',
          activeBasePath: 'docs',
          label: 'Docs',
          position: 'left',
        },
        // {
        //   href: 'https://www.acervera.com/categories/osm4scala/',
        //   label: 'Blog',
        //   position: 'left'
        // },
        {
          href: 'https://github.com/simplexspatial/osm4scala',
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
              to: 'docs/',
            },
          ],
        },
        {
          title: 'Community',
          items: [
            {
              label: 'Stack Overflow',
              href: 'https://stackoverflow.com/search?q=osm4scala',
            },
            {
              label: 'Gitter',
              href: 'https://gitter.im/osm4scala/talk',
            },
            // {
            //   label: 'Twitter',
            //   href: 'https://twitter.com/docusaurus',
            // },
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/simplexspatial/osm4scala',
            },
            {
              label: 'About the author',
              href: 'https://www.acervera.com/',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} <a href="https://www.acervera.com/" target="_blank">Ángel Cervera Claudio</a>. Supported by Simplexportal Ltd.`,
    },
  },
  presets: [
    [
      '@docusaurus/preset-classic',
      {
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          // Please change this to your repo.
          editUrl:
            'https://github.com/simplexspatial/osm4scala/edit/master/website/',
        },
        // blog: {
        //   showReadingTime: true,
        //   // Please change this to your repo.
        //   editUrl:
        //     'https://github.com/facebook/docusaurus/edit/master/website/blog/',
        // },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],
};
