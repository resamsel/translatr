import { promises } from "fs";
import { promise } from "glob-promise";
import { join } from "path";
import { INode, parse, stringify } from "svgson";
import { format, resolveConfig } from "prettier";

const excludedElements = ["metadata", "sodipodi:namedview"];
const excludedAttrs = [
  "xmlns:dc",
  "xmlns:cc",
  "xmlns:rdf",
  "xmlns:svg",
  "xmlns",
  "xmlns:sodipodi",
  "xmlns:inkscape",
  "sodipodi:docname",
  "sodipodi:nodetypes",
  "inkscape:version",
  "inkscape:connector-curvature",
  "inkscape:label"
];

const cleanup = (node: INode): INode | undefined => {
  if (excludedElements.includes(node.name)) {
    return undefined;
  }

  return {
    ...node,
    attributes: Object.entries(node.attributes)
      .filter(([attr]) => !excludedAttrs.includes(attr))
      .reduce(
        (agg, [attr, value]) => ({ ...agg, [attr]: value }),
        {} as Record<string, string>
      )
  };
};

const depthFirst = (
  node: INode,
  operation: (node: INode) => INode
): INode | undefined => {
  const cleanedNode = cleanup(node);

  if (cleanedNode === undefined) {
    return undefined;
  }

  if (cleanedNode.children.length < 1) {
    return cleanedNode;
  }

  return {
    ...cleanedNode,
    children: cleanedNode.children
      .filter(n => Boolean(n))
      .map(n => depthFirst(n, operation))
      .filter(n => Boolean(n))
  };
};

const prettify = (path: string, svg: string): Promise<string> => {
  return resolveConfig(path).then(options =>
    format(svg, { ...options, parser: "html" })
  );
};

const write = (target: string, svg: string): Promise<string> => {
  return promises.writeFile(target, svg).then(() => target);
};

const svgCleanupPath = (path: string): Promise<void[]> => {
  return promise("**/*.inkscape.svg", { cwd: path }).then(files =>
    Promise.all(files.map(file => svgCleanup(join(path, file))))
  );
};

const svgCleanup = (file: string): Promise<void> => {
  const target = file.replace(/\.inkscape\.svg$/, ".svg");
  return promises
    .readFile(file, "utf8")
    .then(data => parse(data))
    .then(data => depthFirst(data, cleanup))
    .then(json => stringify(json, {}))
    .then(svg => prettify(file, svg))
    .then(svg => write(target, svg))
    .then(target => console.log(`Written: ${target}`));
};

if (process.argv.length == 3) {
  svgCleanupPath(process.argv[2]).catch(error => console.error(`${error}`));
} else {
  console.error("Error: no file specified");
}
