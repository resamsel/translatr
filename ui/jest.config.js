module.exports = {
  testEnvironment: 'jsdom',
  testMatch: ['**/+(*.)+(spec|test).+(ts|js)?(x)'],
  transform: {
    '^.+\\.(ts|js|mjs|html)$': ['jest-preset-angular', {
      tsconfig: '<rootDir>/tsconfig.spec.json',
      stringifyContentPathRegex: '\\.html$',
    }],
    '.+\\.(svg)$': 'jest-transform-stub'
  },
  transformIgnorePatterns: ['node_modules/(?!(.*\\.mjs$|d3|d3-array|d3-color|d3-format|d3-interpolate|d3-scale|d3-time|d3-time-format|d3-axis|d3-brush|d3-chord|d3-contour|d3-delaunay|d3-dispatch|d3-drag|d3-dsv|d3-ease|d3-fetch|d3-force|d3-geo|d3-hierarchy|d3-path|d3-polygon|d3-quadtree|d3-random|d3-sankey|d3-scale-chromatic|d3-selection|d3-shape|d3-tile|d3-timer|d3-transition|d3-voronoi|d3-zoom|internmap|delaunator|robust-predicates))'],
  resolver: '@nx/jest/plugins/resolver',
  moduleFileExtensions: ['ts', 'js', 'mjs', 'html'],
  coverageReporters: ['html', 'lcov', 'text'],
  setupFilesAfterEnv: ['<rootDir>/src/test-setup.ts'],
};
