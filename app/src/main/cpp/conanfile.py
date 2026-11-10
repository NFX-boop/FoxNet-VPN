import os
import shutil
import subprocess
import tempfile
from pathlib import Path

from conan import ConanFile
from conan.tools.cmake import CMakeToolchain, CMake, cmake_layout
from conan.tools.files import copy


class FptnLib(ConanFile):
    name = "fptn-lib"
    version = "0.0.0"
    requires = ("nlohmann_json/3.12.0",)
    settings = ("os", "arch", "compiler", "build_type")
    generators = ("CMakeDeps",)
    default_options = {
        "*:fPIC": True,
        "*:shared": False,
        "fptn/*:build_only_fptn_lib": True,
        "fptn/*:with_gui_client": False,
    }

    def requirements(self):
        self._export_fptn_recipe()
        self.requires("fptn/0.0.0@local/local")

    def _export_fptn_recipe(self):
        # The fptn recipe does not list its `.clang-tidy` in `exports_sources`, so a
        # cache build runs clang-tidy (enabled by its CMakeLists whenever a
        # clang-tidy >= 20 binary is found) without a config and fails every file
        # with "no checks enabled". Export the pinned submodule through a staging
        # copy whose recipe adds `.clang-tidy` to `exports_sources`; Conan itself
        # then ships the config via export_source -> source. The submodule and the
        # conan cache are never modified directly.
        script_dir = os.path.dirname(os.path.abspath(__file__))
        submodule_dir = os.path.join(script_dir, "libs", "fptn")
        if not os.path.isdir(submodule_dir):
            raise RuntimeError(
                f"FPTN submodule not found at {submodule_dir}. "
                "Initialize it with: git submodule update --init --recursive"
            )

        clang_tidy_config = os.path.join(submodule_dir, ".clang-tidy")
        if not os.path.isfile(clang_tidy_config):
            raise RuntimeError(
                f"FPTN clang-tidy config not found at {clang_tidy_config}. "
                "The pinned FPTN revision must ship a .clang-tidy file."
            )

        self.output.info(f"Exporting fptn recipe from a staging copy of {submodule_dir}")
        staging_dir = tempfile.mkdtemp(prefix="fptn-recipe-")
        try:
            shutil.copytree(
                submodule_dir,
                staging_dir,
                dirs_exist_ok=True,
                ignore=shutil.ignore_patterns(".git"),
            )
            self._add_clang_tidy_to_exports_sources(staging_dir)
            subprocess.run(
                [
                    "conan",
                    "export",
                    staging_dir,
                    "--name=fptn",
                    "--version=0.0.0",
                    "--user=local",
                    "--channel=local",
                ],
                check=True,
                cwd=script_dir,
            )
        finally:
            shutil.rmtree(staging_dir, ignore_errors=True)

    def _add_clang_tidy_to_exports_sources(self, recipe_dir):
        conanfile_path = os.path.join(recipe_dir, "conanfile.py")
        with open(conanfile_path, encoding="utf-8") as recipe_file:
            content = recipe_file.read()
        exports_sources_marker = "    exports_sources = (\n"
        marker_index = content.find(exports_sources_marker)
        if marker_index == -1:
            raise RuntimeError(f"Unexpected fptn conanfile.py format: {conanfile_path}")
        block_end_index = content.find("\n    )", marker_index)
        if block_end_index == -1:
            raise RuntimeError(f"Unexpected fptn conanfile.py format: {conanfile_path}")
        exports_sources_block = content[marker_index:block_end_index]
        if '".clang-tidy"' not in exports_sources_block:
            patched_block = exports_sources_block.replace(
                exports_sources_marker,
                exports_sources_marker + '        ".clang-tidy",\n',
                1,
            )
            content = content.replace(exports_sources_block, patched_block, 1)
            with open(conanfile_path, "w", encoding="utf-8") as recipe_file:
                recipe_file.write(content)

    def build_requirements(self):
        self.test_requires("gtest/1.17.0")

    def layout(self):
        cmake_layout(self)

    def generate(self):
        tc = CMakeToolchain(self)

        if "fptn" in self.dependencies:
            fptn_dep = self.dependencies["fptn"]
            tc.variables["FPTN_INCLUDE_DIR"] = fptn_dep.cpp_info.includedirs[0] if fptn_dep.cpp_info.includedirs else ""
            tc.variables["FPTN_LIBRARIES"] = fptn_dep.cpp_info.libs[0] if fptn_dep.cpp_info.libs else "fptn"
            if fptn_dep.cpp_info.libdirs:
                tc.variables["FPTN_LIBRARY_DIR"] = fptn_dep.cpp_info.libdirs[0]

        tc.generate()

    def build(self):
        cmake = CMake(self)
        cmake.configure()
        cmake.build()

    def config_options(self):
        pass
