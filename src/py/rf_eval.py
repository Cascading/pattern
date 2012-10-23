#!/usr/bin/env python
# encoding: utf-8

from lxml import etree
import errno
import sys
from xml.dom.minidom import parse, parseString


######################################################################
## global definitions

debug = False # True

ns = {
    'xsi': 'http://www.w3.org/2001/XMLSchema-instance'
    }

textOps = {
    "lessOrEqual": "<=",
    "greaterThan": ">"
}

predicates = []



def resolve_tree (segment, dv_name, iv_list):
    tree_model = segment.getElementsByTagName("TreeModel")[0]
    tree_id = segment.attributes["id"].value
    def_name = "tree_" + tree_id

    print "def " + def_name + " ():"

    node = tree_model.getElementsByTagName("Node")[0]
    resolve_node(tree_id, node, 0, [])

    return def_name


def resolve_node (tree_id, node, depth, trail):
    indent = get_indent(depth)

    try:
        score = node.attributes["score"].value
    except KeyError:
        score = None

    if debug:
        print indent, "node", node.attributes["id"].value, score

    expr_ind = None

    for child_node in node.childNodes:
        if child_node.nodeName == "SimplePredicate":
            expr_ind, predicate = compose_predicate(child_node)
            print indent, predicate

        elif child_node.nodeName == "Node":
            resolve_node(tree_id, child_node, depth + 1, trail + [expr_ind])

    if score:
        indent = get_indent(depth + 1)
        print indent, " ".join(["return", score])
        print indent, "#", tree_id, score, filter(lambda x: x >= 0, trail + [expr_ind])
        print


def get_indent (depth):
    return " ".join(map(lambda x: " ", range(0, depth)))


def compose_predicate (predicate):
    field = predicate.attributes["field"].value
    operator = predicate.attributes["operator"].value
    value = predicate.attributes["value"].value

    #<SimplePredicate field="var3" operator="lessOrEqual" value="0.5"/>

    expression = " ".join([field, textOps[operator], value])

    if expression not in predicates:
        predicates.append(expression)

    expr_ind = predicates.index(expression)
    condition = " ".join(["if", "expr[", str(expr_ind), "]:"])

    return expr_ind, condition


if __name__ == "__main__":
    file_model = sys.argv[1]
    dom = parse(file_model)

    ## generate code for the preamble

    print "import sys"
    print

    ## determine the data dictionary

    data_dict = dom.getElementsByTagName("DataDictionary")[0]

    for data_field in data_dict.getElementsByTagName("DataField"):
        # <DataField name="var1" optype="continuous" dataType="double"/>

        if debug:
            print data_field.attributes["name"].value

    mining_model = dom.getElementsByTagName("MiningModel")[0]

    if debug:
        print mining_model.attributes["modelName"].value

    ## determine the input schema

    dv_name = None
    iv_list = []
    mining_schema = mining_model.getElementsByTagName("MiningSchema")[0]

    for mining_field in mining_schema.getElementsByTagName("MiningField"):
        #<MiningField name="label" usageType="predicted"/>

        if mining_field.attributes["usageType"].value == "predicted":
            dv_name = mining_field.attributes["name"].value
        elif mining_field.attributes["usageType"].value == "active":
            iv_list.append(mining_field.attributes["name"].value)

    ## generate code for each tree

    def_list = []
    segmentation = mining_model.getElementsByTagName("Segmentation")[0]

    for segment in segmentation.getElementsByTagName("Segment"):
        def_name = resolve_tree(segment, dv_name, iv_list)
        def_list.append(def_name)

    ## classify each input tuple

    indent = get_indent(1)
    input_tuple = "( " + ", ".join([dv_name] + iv_list) + " )"

    print "status = { '00': 'TN', '11': 'TP', '10': 'FN', '01': 'FP' }"
    print "confuse = { 'TN': 0, 'TP': 0, 'FN': 0, 'FP': 0 }"
    print "count = -1"
    print
    print "for line in sys.stdin:"
    print indent, "count += 1"

    print
    print indent, "if count > 0:"
    indent = get_indent(2)

    print indent, input_tuple + " = map(lambda x: float(x), line.strip().split('\\t')[0:" + str(len(iv_list) + 1) + "] )"
    print indent, "vote = []"
    print indent, "expr = []"

    for expression in predicates:
        print indent, "expr.append( " + expression + " )"

    input_tuple = "( " + ", ".join(iv_list) + " )"

    for def_name in def_list:
        print indent, "vote.append( " + def_name + "() )"

    print indent, "predict = sum(vote) / float(len(vote))"
    print indent, "print " + ", ".join(iv_list)
    print indent, "print vote"
    print indent, "print label, predict, label == predict"
    print indent, "x = str(int(label)) + str(int(predict))"
    print indent, "confuse[status[x]] += 1"

    print
    print "print confuse"
    print "print 'TN', confuse['TN'] / float(confuse['TN'] + confuse['FP'])"
    print "print 'FP', confuse['FP'] / float(confuse['TN'] + confuse['FP'])"
    print "print 'TP', confuse['TP'] / float(confuse['TP'] + confuse['FN'])"
    print "print 'FN', confuse['FN'] / float(confuse['TP'] + confuse['FN'])"
